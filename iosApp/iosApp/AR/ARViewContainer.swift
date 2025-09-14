//
//  ARViewContainer.swift
//  ArProductFeatureApp
//
//  Created by Глеб Порошин on 23.04.2025.
//
//
//  ARViewContainer.swift
//  ArProductFeatureApp
//
//  Created by Глеб Порошин on 23.04.2025.
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

    func makeUIView(context: Context) -> ARView {
        let arView = ARView(frame: .zero)
        context.coordinator.setup(on: arView)
        return arView
    }

    func updateUIView(_ uiView: ARView, context: Context) {
        if resetRequested && !context.coordinator.shouldReset {
            context.coordinator.requestReset()
        }
        context.coordinator.updateGuidance(showGuidance)
    }

    func makeCoordinator() -> Coordinator {
        let sizeMeters = M3(modelWidthMm, modelHeightMm, modelDepthMm) / 1000
        return Coordinator(
            modelSize:       sizeMeters,
            resetHandler:    onResetRequest,
            showGuidance:    showGuidance,
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
        private var reconstructionEnabled = false
        private let resetHandler: () -> Void
        var shouldReset = false

        // UX
        private var guidanceLabel: UILabel?
        private var lightAnchor: AnchorEntity?

        // Loading
        private var loadCancellable: AnyCancellable?

        // Gestures
        private var tapGR: UITapGestureRecognizer?
        private var longGR: UILongPressGestureRecognizer?
        private var panGR: UIPanGestureRecognizer?
        private var dragRaycast: ARTrackedRaycast?

        // Inputs
        let filePath: String
        private var preloadedModel: ModelEntity?

        init(modelSize: M3,
             resetHandler: @escaping () -> Void,
             showGuidance: Bool,
             filePath: String,
             preloadedModel: ModelEntity?)
        {
            self.modelSize       = modelSize
            self.resetHandler    = resetHandler
            self.showGuidance    = showGuidance
            self.filePath        = filePath
            self.preloadedModel  = preloadedModel
            super.init()
        }

        // MARK: Setup

        func setup(on arView: ARView) {
            self.arView = arView
            arView.session.delegate = self

            // ARKit configuration
            let config = ARWorldTrackingConfiguration()
            config.planeDetection = [.horizontal, .vertical]
            if ARWorldTrackingConfiguration.supportsSceneReconstruction(.meshWithClassification) {
                config.sceneReconstruction = .meshWithClassification
                reconstructionEnabled = true
            } else {
                reconstructionEnabled = false
            }
            config.environmentTexturing = .automatic

            arView.session.run(config, options: [.resetTracking, .removeExistingAnchors])

            // RealityKit scene understanding (occlusion, physics). Avoid receivesLighting for more natural look.
            arView.environment.sceneUnderstanding.options.insert([.occlusion, .physics])

            // UI
            setupGuidanceLabel(in: arView)

            // Gestures
            installGesturesIfNeeded(on: arView)
        }

        func updateGuidance(_ show: Bool) {
            showGuidance = show
            guidanceLabel?.isHidden = !show
        }

        func requestReset() {
            shouldReset = true
            guard let view = arView else { return }

            // Cancel async ops
            loadCancellable?.cancel(); loadCancellable = nil
            dragRaycast?.stopTracking(); dragRaycast = nil

            // Clear scene
            view.session.pause()
            view.scene.anchors.removeAll()
            lightAnchor = nil
            modelAnchor = nil
            modelEntity = nil

            // Re-run configuration fresh
            setup(on: view)

            shouldReset = false
            resetHandler()
        }

        // MARK: Gestures

        private func installGesturesIfNeeded(on arView: ARView) {
            // Remove our previous recognizers (if any)
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

        // Allow long press + pan simultaneously; don't block RealityKit gestures (rotation)
        func gestureRecognizer(_ g: UIGestureRecognizer,
                               shouldRecognizeSimultaneouslyWith other: UIGestureRecognizer) -> Bool
        {
            let isLongPanPair = (g === longGR && other === panGR) || (g === panGR && other === longGR)
            return isLongPanPair
        }

        // MARK: Tap / Placement

        @objc private func handleTap(_ gesture: UITapGestureRecognizer) {
            guard let arView = arView else { return }
            let pt = gesture.location(in: arView)

            // Prefer existing plane geometry → then estimated
            if let query = arView.makeRaycastQuery(from: pt, allowing: .existingPlaneGeometry, alignment: .horizontal),
               let result = arView.session.raycast(query).first {
                placeModel(using: result)
                return
            }
            if let query = arView.makeRaycastQuery(from: pt, allowing: .estimatedPlane, alignment: .horizontal),
               let result = arView.session.raycast(query).first {
                placeModel(using: result)
            }
        }

        private func placeModel(using result: ARRaycastResult) {
            guard let arView = arView else { return }

            let anchor = AnchorEntity(raycastResult: result)
            self.modelAnchor = anchor
            arView.scene.addAnchor(anchor)

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
                // Start tracked raycast (auto-refines while user moves the device)
                if let q = arView.makeRaycastQuery(from: loc, allowing: .existingPlaneGeometry, alignment: .horizontal) {
                    dragRaycast = arView.session.trackedRaycast(q) { [weak self] results in
                        guard let hit = results.first, let self = self else { return }
                        anchor.move(to: Transform(matrix: hit.worldTransform), relativeTo: nil)
                    }
                }
            case .changed:
                // If trackedRaycast wasn't created (no existing plane), fallback to estimated plane
                if dragRaycast == nil,
                   let hit = arView.raycast(from: loc, allowing: .estimatedPlane, alignment: .horizontal).first {
                    anchor.move(to: Transform(matrix: hit.worldTransform), relativeTo: nil)
                }
            case .ended, .cancelled, .failed:
                dragRaycast?.stopTracking()
                dragRaycast = nil
            default: break
            }
        }

        // MARK: Model loading & configuration

        private func loadAndConfigureModel(into anchor: AnchorEntity, in arView: ARView) {
            updateGuidanceLabel(text: "Загрузка модели…")

            if let preloaded = preloadedModel {
                configure(entity: preloaded, on: anchor, in: arView)
                updateGuidanceLabel(text: "Тапните для перемещения. Долгое нажатие + перетаскивание.")
                return
            }

            let url = resolveURL(from: filePath)
            // Async load to avoid UI hitches
            loadCancellable?.cancel()
            loadCancellable = Entity.loadModelAsync(contentsOf: url)
                .receive(on: RunLoop.main)
                .sink(receiveCompletion: { [weak self] completion in
                    guard let self = self else { return }
                    if case .failure(let error) = completion {
                        self.updateGuidanceLabel(text: "Ошибка загрузки: \(error.localizedDescription)")
                    }
                }, receiveValue: { [weak self] entity in
                    guard let self = self else { return }
                    self.configure(entity: entity, on: anchor, in: arView)
                    self.updateGuidanceLabel(text: "Тапните для перемещения. Долгое нажатие + перетаскивание.")
                })
        }

        private func configure(entity: ModelEntity, on anchor: AnchorEntity, in arView: ARView) {
            // Add first so bounds below can be computed in anchor space
            anchor.addChild(entity)

            // Collision (for gestures / physics)
            entity.generateCollisionShapes(recursive: true)

            // Ensure collisions resolve with scene-understanding meshes and other entities
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

            // Physics: dynamic only when real-world mesh is available; otherwise kinematic
            if reconstructionEnabled {
                entity.physicsBody = .init(mode: .dynamic)
            } else {
                entity.physicsBody = .init(mode: .kinematic)
            }

            // Scale uniformly to target size and snap to floor (anchor’s Y=0)
            fitModel(entity, to: modelSize, relativeTo: anchor, uniform: true, snapToFloor: true)

            // User rotation gesture from RealityKit
            arView.installGestures([.rotation], for: entity)

            self.modelEntity = entity
        }

        private func resolveURL(from path: String) -> URL {
            if path.hasPrefix("/") {
                return URL(fileURLWithPath: path)
            }
            let cachesURL = FileManager.default.urls(for: .cachesDirectory, in: .userDomainMask).first!
            return cachesURL.appendingPathComponent(path)
        }

        /// Uniformly fits model to target size (meters) and optionally snaps its bottom to Y=0 in `relativeTo` space.
        private func fitModel(_ entity: ModelEntity,
                              to targetMeters: SIMD3<Float>,
                              relativeTo ref: Entity,
                              uniform: Bool = true,
                              snapToFloor: Bool = true)
        {
            // Reset scale to measure original bounds in this space
            entity.scale = .one
            let b0 = entity.visualBounds(recursive: true, relativeTo: ref)
            let size0 = b0.max - b0.min

            let sx = targetMeters.x / max(size0.x, 1e-3)
            let sy = targetMeters.y / max(size0.y, 1e-3)
            let sz = targetMeters.z / max(size0.z, 1e-3)

            if uniform {
                let s = min(sx, min(sy, sz))
                entity.scale = M3(repeating: s)
            } else {
                entity.scale = M3(sx, sy, sz)
            }

            if snapToFloor {
                let b1 = entity.visualBounds(recursive: true, relativeTo: ref)
                let lift = -b1.min.y
                entity.position.y += lift
            }
        }

        // MARK: Lighting

        private func addSunLight(to arView: ARView) {
            // Remove prior light anchor if any
            if let lightAnchor = lightAnchor {
                arView.scene.removeAnchor(lightAnchor)
            }

            let sun = DirectionalLight()
            sun.light.color = .white
            sun.light.intensity = 25_000
            sun.shadow = DirectionalLightComponent.Shadow(maximumDistance: 5, depthBias: 1e-4)

            let la = AnchorEntity(world: simd_float4x4(1))
            sun.position = [0, 2, 0]
            sun.look(at: .zero, from: sun.position, relativeTo: la)

            la.addChild(sun)
            arView.scene.addAnchor(la)
            self.lightAnchor = la
        }

        // MARK: Guidance UI

        private func setupGuidanceLabel(in arView: ARView) {
            if let existing = guidanceLabel {
                existing.isHidden = !showGuidance
                existing.text = existing.text?.isEmpty == false ? existing.text
                                : "Тапните для размещения, долгое нажатие + перетаскивание"
                return
            }

            let label = UILabel()
            label.text = "Тапните для размещения, долгое нажатие + перетаскивание"
            label.textAlignment = .center
            label.textColor = .white
            label.font = .systemFont(ofSize: 16, weight: .medium)
            label.backgroundColor = UIColor.black.withAlphaComponent(0.6)
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
    }
}
