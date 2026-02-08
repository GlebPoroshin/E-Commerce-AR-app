//
//  ARViewContainer.swift
//  iosApp
//
//  Created by Глеб Порошин on 14.09.2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI
import RealityKit
import ARKit
import Combine

typealias M3 = SIMD3<Float>

struct ARViewContainer: UIViewRepresentable {
    let filePath: String
    let preloadedModel: ModelEntity?

    let modelWidthMm:  Float
    let modelHeightMm: Float
    let modelDepthMm:  Float

    var onResetRequest: () -> Void
    var resetRequested: Bool
    var showGuidance: Bool = true
    var isSingleMode: Bool = true

    func makeUIView(context: Context) -> ARView {
        let arView = ARView(frame: .zero)

        // Render options tweaks (fewer artifacts, better readability)
        arView.renderOptions.remove([.disableDepthOfField, .disableMotionBlur])

        context.coordinator.setup(on: arView)
        return arView
    }

    func updateUIView(_ uiView: ARView, context: Context) {
        if resetRequested && !context.coordinator.shouldReset {
            context.coordinator.requestReset()
        }
        context.coordinator.updateGuidance(showGuidance)
        context.coordinator.setSingleMode(isSingleMode)
    }

    func makeCoordinator() -> Coordinator {
        let sizeMeters = M3(modelWidthMm, modelHeightMm, modelDepthMm) / 1000
        return Coordinator(
            modelSize:       sizeMeters,
            resetHandler:    onResetRequest,
            showGuidance:    showGuidance,
            singleMode:      isSingleMode,
            filePath:        filePath,
            preloadedModel:  preloadedModel
        )
    }

    // MARK: - Coordinator

    class Coordinator: NSObject, ARSessionDelegate, UIGestureRecognizerDelegate {
        // Core
        private weak var arView: ARView?
        private var modelAnchor: AnchorEntity?
        private var modelEntity: ModelEntity?
        private var modelSize: M3
        private var showGuidance: Bool
        private var isSingleMode: Bool
        private var reconstructionEnabled = false
        private let resetHandler: () -> Void
        var shouldReset = false

        // Current plane alignment for dragging (lock after placement)
        private var placementAlignment: ARRaycastQuery.TargetAlignment = .horizontal

        // UX
        private var guidanceLabel: UILabel?
        private var coachingOverlay: ARCoachingOverlayView?

        // Loading
        private var loadCancellable: AnyCancellable?

        // Gestures
        private var tapGR: UITapGestureRecognizer?
        private var longGR: UILongPressGestureRecognizer?
        private var panGR: UIPanGestureRecognizer?
        private var dragRaycast: ARTrackedRaycast?
        private var placedAnchors: [AnchorEntity] = []
        private var placedEntities: [ModelEntity] = []

        // Inputs
        let filePath: String
        private var preloadedModel: ModelEntity?

        // Safe placement distance range (meters)
        private let minPlaceDistance: Float = 0.35
        private let maxPlaceDistance: Float = 4.0

        init(modelSize: M3,
             resetHandler: @escaping () -> Void,
             showGuidance: Bool,
             singleMode: Bool,
             filePath: String,
             preloadedModel: ModelEntity?)
        {
            self.modelSize       = modelSize
            self.resetHandler    = resetHandler
            self.showGuidance    = showGuidance
            self.isSingleMode    = singleMode
            self.filePath        = filePath
            self.preloadedModel  = preloadedModel
            super.init()
        }

        // MARK: Setup

        func setup(on arView: ARView) {
            self.arView = arView
            arView.session.delegate = self

            // Basic configuration
            let config = ARWorldTrackingConfiguration()
            config.worldAlignment = .gravity
            config.planeDetection = [.horizontal]

            // Automatic lighting from camera:
            // - brightness/temperature estimation (Light Estimation)
            // - environment map generation (Environment Texturing)
            config.isLightEstimationEnabled = true
            config.environmentTexturing = .automatic

            // LiDAR: scene mesh for occlusion/collisions (only on supported devices)
            if ARWorldTrackingConfiguration.supportsSceneReconstruction(.meshWithClassification) {
                config.sceneReconstruction = .meshWithClassification
                reconstructionEnabled = true
            } else {
                reconstructionEnabled = false
            }

            // People Occlusion (works without LiDAR on supported A12+ devices)
            if ARWorldTrackingConfiguration.supportsFrameSemantics(.personSegmentationWithDepth) {
                config.frameSemantics.insert(.personSegmentationWithDepth)
            }

            // Start session
            arView.session.run(config, options: [.resetTracking, .removeExistingAnchors])

            // Occlusion/Physics/Lighting from scene understanding:
            // - with LiDAR: real geometry provides occlusion, collisions, and receives lighting
            // - without LiDAR: options are safe; People Occlusion still applies where supported
            arView.environment.sceneUnderstanding.options.insert([.occlusion, .receivesLighting, .physics])

            // UI
            setupGuidanceLabel(in: arView)
            setupCoachingOverlay(in: arView)

            // Gestures
            installGesturesIfNeeded(on: arView)
        }


        func updateGuidance(_ show: Bool) {
            showGuidance = show
            guidanceLabel?.isHidden = !show
        }

        func setSingleMode(_ single: Bool) {
            guard isSingleMode != single else { return }
            isSingleMode = single
            if single {
                collapseToSingleIfNeeded()
            }
        }

        func requestReset() {
            shouldReset = true
            guard let view = arView else { return }

            // Cancel async operations
            loadCancellable?.cancel(); loadCancellable = nil
            dragRaycast?.stopTracking(); dragRaycast = nil

            // Clear scene
            view.session.pause()
            view.scene.anchors.removeAll()
            modelAnchor = nil
            modelEntity = nil
            placedAnchors.removeAll()
            placedEntities.removeAll()

            // Relaunch configuration fresh
            setup(on: view)

            shouldReset = false
            resetHandler()
        }

        // MARK: Gestures

        private func installGesturesIfNeeded(on arView: ARView) {
            [tapGR, longGR, panGR].compactMap { $0 }.forEach { arView.removeGestureRecognizer($0) }

            let tap = UITapGestureRecognizer(target: self, action: #selector(handleTap(_:)))
            tap.delegate = self
            arView.addGestureRecognizer(tap)
            tapGR = tap

            let long = UILongPressGestureRecognizer(target: self, action: #selector(handleLongPress(_:)))
            long.minimumPressDuration = 0.25
            long.delegate = self
            arView.addGestureRecognizer(long)
            longGR = long

            let pan = UIPanGestureRecognizer(target: self, action: #selector(handleDrag(_:)))
            pan.minimumNumberOfTouches = 1
            pan.maximumNumberOfTouches = 1
            pan.delegate = self
            arView.addGestureRecognizer(pan)
            panGR = pan
        }

        // Allow long press + pan simultaneously (do not block RealityKit rotation gesture)
        func gestureRecognizer(_ g: UIGestureRecognizer,
                               shouldRecognizeSimultaneouslyWith other: UIGestureRecognizer) -> Bool
        {
            let isLongPanPair = (g === longGR && other === panGR) || (g === panGR && other === longGR)
            return isLongPanPair
        }

        // MARK: Tap / Placement

        @objc private func handleTap(_ gesture: UITapGestureRecognizer) {
            guard let arView = arView else { return }
            guard trackingIsReliable() else {
                updateGuidanceLabel(text: "Point camera and move device to improve tracking")
                return
            }

            let pt = gesture.location(in: arView)

            // 1) Strict real geometry hit test (best quality)
            if let hit = raycast(at: pt, in: arView,
                                 allowing: .existingPlaneGeometry,
                                 alignment: .horizontal)
            {
                placeOrMoveModel(using: hit, in: arView)
                return
            }

            // 2) Fallback: estimated plane
            if let hit = raycast(at: pt, in: arView,
                                 allowing: .estimatedPlane,
                                 alignment: .horizontal)
            {
                placeOrMoveModel(using: hit, in: arView)
                return
            }

            updateGuidanceLabel(text: "Surface not found. Try different angle/lighting")
        }

        private func placeOrMoveModel(using result: ARRaycastResult, in arView: ARView) {
            // Store plane alignment for subsequent drag
            placementAlignment = .horizontal

            // Clamp placement distance (safety/readability)
            let clamped = clampedTransform(for: result, in: arView)

            if isSingleMode, let anchor = modelAnchor, let entity = modelEntity {
                let originalScale = entity.scale
                // Reuse existing anchor → smooth move
                anchor.move(to: Transform(matrix: clamped), relativeTo: nil, duration: 0.06, timingFunction: .easeInOut)
                
                if let e = self.modelEntity, let v = self.arView {
                    self.groundRaycastSnap(e, in: v, alignment: self.placementAlignment)
                }
                
                // After moving – realign orientation and keep contact with the plane
                alignEntityToPlane(entity, with: clamped)
                snapEntityToPlane(entity, relativeTo: anchor)
                if entity.scale != originalScale {
                    entity.scale = originalScale
                    snapEntityToPlane(entity, relativeTo: anchor)
                }
                updateGuidanceLabel(text: "Long press + drag for precise adjustment")
                return
            }

            if isSingleMode {
                clearAllPlaced()
            }

            // First-time placement
            let anchor = AnchorEntity(world: clamped)
            self.modelAnchor = anchor
            arView.scene.addAnchor(anchor)
            placedAnchors.append(anchor)

            loadAndConfigureModel(into: anchor, in: arView)
        }

        // MARK: Selection & Drag

        @objc private func handleLongPress(_ gesture: UILongPressGestureRecognizer) {
            guard let arView = arView else { return }
            let loc = gesture.location(in: arView)

            switch gesture.state {
            case .began:
                if let entity = arView.entity(at: loc) as? ModelEntity {
                    modelEntity = entity
                }
            case .ended, .cancelled, .failed:
                dragRaycast?.stopTracking()
                dragRaycast = nil
                modelEntity = nil
            default: break
            }
        }

        @objc private func handleDrag(_ gesture: UIPanGestureRecognizer) {
            guard let arView = arView,
                  let entity = modelEntity,
                  let anchor = entity.anchor else { return }

            let loc = gesture.location(in: arView)

            switch gesture.state {
            case .began:
                guard trackingIsReliable() else { return }
                // trackedRaycast on locked plane alignment (prevents jumping between wall/floor)
                if let q = arView.makeRaycastQuery(from: loc, allowing: .existingPlaneGeometry, alignment: placementAlignment) {
                    dragRaycast = arView.session.trackedRaycast(q) { [weak self] results in
                        guard let self = self, let arView = self.arView, let hit = results.first else { return }
                        let t = self.clampedTransform(for: hit, in: arView)
                        anchor.move(to: Transform(matrix: t), relativeTo: nil, duration: 0.03, timingFunction: .linear)
                        // Maintain orientation and plane contact while moving
                        self.alignEntityToPlane(entity, with: t)
                        self.snapEntityToPlane(entity, relativeTo: anchor)
                    }
                }
            case .changed:
                if dragRaycast == nil,
                   let hit = arView.raycast(from: loc, allowing: .estimatedPlane, alignment: placementAlignment).first {
                    let t = clampedTransform(for: hit, in: arView)
                    anchor.move(to: Transform(matrix: t), relativeTo: nil, duration: 0.03, timingFunction: .linear)
                    alignEntityToPlane(entity, with: t)
                    snapEntityToPlane(entity, relativeTo: anchor)
                }
            case .ended, .cancelled, .failed:
                dragRaycast?.stopTracking(); dragRaycast = nil
            default: break
            }
        }

        // MARK: Model loading & configuration

        private func loadAndConfigureModel(into anchor: AnchorEntity, in arView: ARView) {
            updateGuidanceLabel(text: "Loading model…")

            if let preloaded = preloadedModel?.clone(recursive: true) {
                preloaded.transform = .identity
                configure(entity: preloaded, on: anchor, in: arView)
                updateGuidanceLabel(text: "Tap to move. Long press + drag.")
                return
            }

            let url = resolveURL(from: filePath)
            loadCancellable?.cancel()
            loadCancellable = Entity.loadModelAsync(contentsOf: url)
                .receive(on: RunLoop.main)
                .sink(receiveCompletion: { [weak self] completion in
                    guard let self = self else { return }
                    if case .failure(let error) = completion {
                        self.updateGuidanceLabel(text: "Loading error: \(error.localizedDescription)")
                    }
                }, receiveValue: { [weak self] entity in
                    guard let self = self else { return }
                    entity.transform = .identity
                    self.configure(entity: entity, on: anchor, in: arView)
                    self.updateGuidanceLabel(text: "Tap to move. Long press + drag.")
                })
        }

        private func configure(entity: ModelEntity, on anchor: AnchorEntity, in arView: ARView) {
            entity.transform = .identity
            // Add first so bounds are computed in anchor space
            anchor.addChild(entity)

            // Collision shapes (for gestures/physics) + interaction with scene-understanding mesh
            entity.generateCollisionShapes(recursive: true)
            if var collision = entity.components[CollisionComponent.self] {
                collision.filter = CollisionFilter(group: .default, mask: [.default, .sceneUnderstanding])
                entity.components[CollisionComponent.self] = collision
            } else {
                entity.components[CollisionComponent.self] = CollisionComponent(
                    shapes: [],
                    mode: .default,
                    filter: CollisionFilter(group: .default, mask: [.default, .sceneUnderstanding])
                )
            }

            // Physics: kinematic to avoid tipping on uneven reconstruction
            entity.physicsBody = .init(mode: .kinematic)

            // Scale 1:1 to target size (meters), snap to plane
            fitModel(entity, to: modelSize, relativeTo: anchor, uniform: true, snapToPlane: true)

            // Orientation: align model up axis with plane normal
            alignEntityToPlane(entity, with: anchor.transformMatrix(relativeTo: nil))

            // RealityKit rotation gesture (rotation only — no scaling)
            arView.installGestures([.rotation], for: entity)

            self.modelEntity = entity
            placedEntities.append(entity)

            // Shadow light is configured in setup and updated from light estimate.
        }

        private func resolveURL(from path: String) -> URL {
            if path.hasPrefix("/") {
                return URL(fileURLWithPath: path)
            }
            let cachesURL = FileManager.default.urls(for: .cachesDirectory, in: .userDomainMask).first!
            return cachesURL.appendingPathComponent(path)
        }

        // MARK: Fitting / Alignment

        /// Fit the model to real-world target size and press it against the plane.
        private func fitModel(_ entity: ModelEntity,
                              to targetMeters: SIMD3<Float>,
                              relativeTo ref: Entity,
                              uniform: Bool = true,
                              snapToPlane: Bool = true)
        {
            entity.scale = .one
            let b0 = entity.visualBounds(recursive: true, relativeTo: ref)
            let size0 = b0.max - b0.min

            let sx = targetMeters.x / max(size0.x, 1e-3)
            let sy = targetMeters.y / max(size0.y, 1e-3)
            let sz = targetMeters.z / max(size0.z, 1e-3)

            if uniform {
                let s = max(0.01 as Float, min(100.0 as Float, (sx + sy + sz) / 3.0))
                entity.scale = M3(repeating: s)
            } else {
                entity.scale = M3(sx, sy, sz)
            }

            if snapToPlane {
                snapEntityToPlane(entity, relativeTo: ref)
            }
        }

        /// Press the model's bottom face to the reference plane (epsilon offsets z-fighting).
        private func snapEntityToPlane(_ entity: ModelEntity, relativeTo ref: Entity) {
            let b1 = entity.visualBounds(recursive: true, relativeTo: ref)
            let epsilon: Float = 0.002
            let lift = -(b1.min.y - epsilon)
            entity.position.y += lift
        }

        /// Align orientation so the model's local up axis matches the plane up (or wall normal).
        private func alignEntityToPlane(_ entity: ModelEntity, with planeTransform: simd_float4x4) {
            // Extract world up from the plane transform matrix
            let upWorld = normalize(SIMD3<Float>(planeTransform.columns.1.x,
                                                 planeTransform.columns.1.y,
                                                 planeTransform.columns.1.z))
            // Current model up
            let c1 = entity.transform.matrix.columns.1
            let currentUp = normalize(SIMD3<Float>(c1.x, c1.y, c1.z))
            let axis = simd_normalize(simd_cross(currentUp, upWorld))
            let dot = max(-1.0, min(1.0, simd_dot(currentUp, upWorld)))
            let angle = acos(dot)
            if angle.isFinite && angle > 1e-3 {
                let q = simd_quatf(angle: angle, axis: axis)
                entity.orientation = q * entity.orientation
            }
            // For vertical planes you can additionally force the “back” face toward the normal if needed.
        }

        // MARK: Raycast helpers

        private func raycast(at pt: CGPoint,
                             in arView: ARView,
                             allowing: ARRaycastQuery.Target,
                             alignment: ARRaycastQuery.TargetAlignment) -> ARRaycastResult?
        {
            // Pick the lowest surface. Prefer classified floor hits when available.
            guard let query = arView.makeRaycastQuery(from: pt, allowing: allowing, alignment: alignment) else { return nil }
            let results = arView.session.raycast(query)
            guard !results.isEmpty else { return nil }

            let floorResults = results.filter { ($0.anchor as? ARPlaneAnchor)?.classification == .floor }
            let candidates = floorResults.isEmpty ? results : floorResults
            return candidates.min(by: { $0.worldTransform.columns.3.y < $1.worldTransform.columns.3.y }) ?? candidates.first
        }

        private func clampedTransform(for result: ARRaycastResult, in arView: ARView) -> simd_float4x4 {
            let t = result.worldTransform
            guard let cam = arView.session.currentFrame?.camera.transform else { return t }

            let camPos = SIMD3<Float>(cam.columns.3.x, cam.columns.3.y, cam.columns.3.z)
            let hitPos = SIMD3<Float>(t.columns.3.x, t.columns.3.y, t.columns.3.z)
            var dir = hitPos - camPos
            var dist = simd_length(dir)
            if dist < 1e-4 { return t }
            dir /= dist

            dist = max(minPlaceDistance, min(maxPlaceDistance, dist))
            let newPos = camPos + dir * dist

            var out = t
            out.columns.3 = SIMD4<Float>(newPos.x, newPos.y, newPos.z, 1)
            return out
        }

        private func trackingIsReliable() -> Bool {
            guard let frame = arView?.session.currentFrame else { return false }
            switch frame.camera.trackingState {
            case .normal: return true
            case .limited: return false
            @unknown default: return false
            }
        }

        // MARK: Placement bookkeeping

        private func collapseToSingleIfNeeded() {
            guard placedAnchors.count > 1 else { return }
            let keepAnchor = placedAnchors.last
            for anchor in placedAnchors.dropLast() {
                removeAnchor(anchor)
            }
            placedAnchors = keepAnchor.map { [$0] } ?? []

            let keepEntity = keepAnchor.flatMap { anchor in
                placedEntities.first { $0.anchor == anchor }
            }
            placedEntities = keepEntity.map { [$0] } ?? []
            modelAnchor = keepAnchor
            modelEntity = keepEntity
        }

        private func clearAllPlaced() {
            for anchor in placedAnchors {
                removeAnchor(anchor)
            }
            placedAnchors.removeAll()
            placedEntities.removeAll()
            modelAnchor = nil
            modelEntity = nil
        }

        private func removeAnchor(_ anchor: AnchorEntity) {
            if let view = arView {
                view.scene.removeAnchor(anchor)
            } else {
                anchor.removeFromParent()
            }
        }

        // MARK: Guidance UI

        private func setupGuidanceLabel(in arView: ARView) {
            if let existing = guidanceLabel {
                existing.isHidden = !showGuidance
                if (existing.text?.isEmpty ?? true) {
                    existing.text = "Tap to place. Long press + drag. Two fingers — rotate."
                }
                return
            }

            let label = UILabel()
            label.text = "Tap to place. Long press + drag. Two fingers — rotate."
            label.textAlignment = .center
            label.textColor = .white
            label.font = .systemFont(ofSize: 15, weight: .medium)
            label.backgroundColor = UIColor.black.withAlphaComponent(0.55)
            label.layer.cornerRadius = 8
            label.clipsToBounds = true
            label.numberOfLines = 0
            label.isHidden = !showGuidance
            label.translatesAutoresizingMaskIntoConstraints = false

            arView.addSubview(label)
            NSLayoutConstraint.activate([
                label.bottomAnchor.constraint(equalTo: arView.bottomAnchor, constant: -50),
                label.centerXAnchor.constraint(equalTo: arView.centerXAnchor),
                label.widthAnchor.constraint(lessThanOrEqualTo: arView.widthAnchor, constant: -40)
            ])
            guidanceLabel = label
        }

        private func updateGuidanceLabel(text: String) {
            guidanceLabel?.text = text
        }

        private func setupCoachingOverlay(in arView: ARView) {
            let overlay = ARCoachingOverlayView()
            overlay.session = arView.session
            overlay.autoresizingMask = [.flexibleWidth, .flexibleHeight]
            overlay.goal = .horizontalPlane
            overlay.activatesAutomatically = true
            overlay.frame = arView.bounds
            arView.addSubview(overlay)
            coachingOverlay = overlay
        }
        
        /// Hard snap model to plane below (floor/wall), considering its real bottom.
        private func groundRaycastSnap(_ entity: ModelEntity,
                                       in arView: ARView,
                                       alignment: ARRaycastQuery.TargetAlignment) {
            guard let anchor = entity.anchor else { return }

            // 1) temporarily remove scene collisions and physics body — so nothing "props up"
            let oldPhysics = entity.physicsBody
            entity.physicsBody = nil

            let oldCollision = entity.components[CollisionComponent.self]
            if var c = oldCollision {
                c.filter.mask.remove(.sceneUnderstanding)  // so LiDAR mesh doesn't interfere
                entity.components[CollisionComponent.self] = c
            }

            defer {
                // restore as it was
                entity.physicsBody = oldPhysics
                if let old = oldCollision {
                    entity.components[CollisionComponent.self] = old
                }
            }

            // 2) ray downward from current model position (slightly higher to guarantee intersection)
            let worldT = entity.transformMatrix(relativeTo: nil)
            let origin = SIMD3<Float>(worldT.columns.3.x, worldT.columns.3.y + 0.5, worldT.columns.3.z)
            let dir    = SIMD3<Float>(0, -1, 0)

            // first — by real geometry, then fallback by estimated plane
            let hit: ARRaycastResult? = {
                // 1) existingPlaneGeometry
                let q1 = ARRaycastQuery(origin: origin,
                                        direction: dir,
                                        allowing: .existingPlaneGeometry,
                                        alignment: alignment)
                if let first = arView.session.raycast(q1).first { return first }

                // 2) estimatedPlane (fallback)
                let q2 = ARRaycastQuery(origin: origin,
                                        direction: dir,
                                        allowing: .estimatedPlane,
                                        alignment: alignment)
                return arView.session.raycast(q2).first
            }()


            guard let h = hit else { return }

            // 3) shift so bottom coincides with plane height + ε
            let bounds = entity.visualBounds(recursive: true, relativeTo: anchor)
            let bottomToOrigin = bounds.min.y             // where bottom face is relative to anchor
            let planeY = h.worldTransform.columns.3.y
            let currentY = entity.position(relativeTo: nil).y
            let epsilon: Float = 0.002

            // how much to lower/raise:
            let deltaY = (planeY - (currentY + bottomToOrigin)) + epsilon
            entity.position.y += deltaY
        }
    }
}
