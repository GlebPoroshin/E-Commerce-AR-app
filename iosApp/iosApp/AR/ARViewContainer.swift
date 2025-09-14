//
//  ARViewContainer.swift
//  ArProductFeatureApp
//
//  Created by Глеб Порошин on 23.04.2025.
//

import SwiftUI
import RealityKit
import ARKit

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
            modelSize:    sizeMeters,
            resetHandler: onResetRequest,
            showGuidance: showGuidance,
            filePath: filePath,
            preloadedModel: preloadedModel
        )
    }

    class Coordinator: NSObject, ARSessionDelegate {
        private weak var arView: ARView?
        private var guidanceLabel: UILabel?
        private var modelEntity: ModelEntity?
        private var modelSize: M3
        private var showGuidance: Bool
        private var planeDetectionTimer: Timer?
        var shouldReset = false
        private let resetHandler: () -> Void
        
        let filePath: String
        private var preloadedModel: ModelEntity?

        init(modelSize: M3,
             resetHandler: @escaping () -> Void,
             showGuidance: Bool,
             filePath: String,
             preloadedModel: ModelEntity?)
        {
            self.modelSize    = modelSize
            self.resetHandler = resetHandler
            self.showGuidance = showGuidance
            self.filePath     = filePath
            self.preloadedModel = preloadedModel
            super.init()
        }

        func setup(on arView: ARView) {
            self.arView = arView
            arView.session.delegate = self

            let config = ARWorldTrackingConfiguration()
            config.planeDetection = [.horizontal, .vertical]
            config.environmentTexturing = .automatic
            if ARWorldTrackingConfiguration.supportsSceneReconstruction(.meshWithClassification) {
                config.sceneReconstruction = .meshWithClassification
            }
            if ARWorldTrackingConfiguration.supportsFrameSemantics(.sceneDepth) {
                config.frameSemantics.insert(.sceneDepth)
            }
            if ARWorldTrackingConfiguration.supportsFrameSemantics(.smoothedSceneDepth) {
                config.frameSemantics.insert(.smoothedSceneDepth)
            }
            arView.session.run(
                config,
                options: [.resetTracking, .removeExistingAnchors]
            )

            arView.environment.sceneUnderstanding.options = [.occlusion, .physics]

            setupGuidanceLabel(in: arView)

            // Tap to place
            let tapGR = UITapGestureRecognizer(
                target: self,
                action: #selector(handleTap(_:))
            )
            arView.addGestureRecognizer(tapGR)

            // Long press to select (temporarily disabled)
            // let longGR = UILongPressGestureRecognizer(target: self, action: #selector(handleLongPress(_:)))
            // arView.addGestureRecognizer(longGR)

            // Pan to drag selected
            let panGR = UIPanGestureRecognizer(target: self, action: #selector(handleDrag(_:)))
            
            // panGR.require(toFail: longGR)
            panGR.minimumNumberOfTouches = 1
            panGR.maximumNumberOfTouches = 1
            arView.addGestureRecognizer(panGR)
        }

        func updateGuidance(_ show: Bool) {
            showGuidance = show
            guidanceLabel?.isHidden = !show
        }

        func requestReset() {
            shouldReset = true
            if let view = arView {
                view.scene.anchors.removeAll()
                modelEntity = nil
                shouldReset = false
                setup(on: view)
            }
            resetHandler()
        }

        // MARK: Tap / Placement

        @objc private func handleTap(_ gesture: UITapGestureRecognizer) {
            guard let arView = arView else { return }
            let pt = gesture.location(in: arView)

            if let query = arView.makeRaycastQuery(
                 from: pt,
                 allowing: .existingPlaneGeometry,
                 alignment: .horizontal
               ),
               let result = arView.session.raycast(query).first
            {
                let anchor = AnchorEntity(raycastResult: result)
                placeModel(on: anchor)
                return
            }

            if let query = arView.makeRaycastQuery(
                 from: pt,
                 allowing: .estimatedPlane,
                 alignment: .horizontal
               ),
               let result = arView.session.raycast(query).first
            {
                let anchor = AnchorEntity(raycastResult: result)
                placeModel(on: anchor)
            }
        }

        // MARK: Selection & Drag
        /*
        // Long press selection temporarily disabled
        @objc private func handleLongPress(_ gesture: UILongPressGestureRecognizer) {
            guard let arView = arView else { return }
            let loc = gesture.location(in: arView)
            if gesture.state == .began,
               let entity = arView.entity(at: loc) as? ModelEntity {
                modelEntity = entity
            } else if gesture.state == .ended || gesture.state == .cancelled {
                modelEntity = nil
            }
        }
        */

        @objc private func handleDrag(_ gesture: UIPanGestureRecognizer) {
            guard let arView = arView,
                  let entity = modelEntity else { return }
            let loc = gesture.location(in: arView)
            if gesture.state == .changed {
                if let hit = arView.raycast(
                    from: loc,
                    allowing: .estimatedPlane,
                    alignment: .horizontal
                ).first {
                    var t = hit.worldTransform
                    t.columns.3.y = entity.position.y
                    entity.move(to: Transform(matrix: t), relativeTo: nil)
                }
            }
        }

        // MARK: Model placement

        private func placeModel(on anchor: AnchorEntity) {
            guard let arView = arView else { return }
            loadAndConfigureModel(into: anchor, in: arView)
        }

        private func loadAndConfigureModel(
            into anchor: AnchorEntity,
            in arView: ARView
        ) {
            do {
                let entity: ModelEntity
                if let preloaded = preloadedModel {
                    entity = preloaded
                } else {
                    let url = resolveURL(from: filePath)
                    entity = try Entity.loadModel(contentsOf: url)
                }
                entity.generateCollisionShapes(recursive: true)
                scaleModel(entity)
                anchor.addChild(entity)
                alignBottom(entity, relativeTo: anchor)
                arView.scene.addAnchor(anchor)
                arView.installGestures([.rotation], for: entity)
                modelEntity = entity
            } catch {
                updateGuidanceLabel(text: "Ошибка загрузки: \(error.localizedDescription)")
            }
        }

        private func resolveURL(from path: String) -> URL {
            if path.hasPrefix("/") {
                return URL(fileURLWithPath: path)
            }
            let cachesURL = FileManager.default.urls(for: .cachesDirectory, in: .userDomainMask).first!
            return cachesURL.appendingPathComponent(path)
        }

        private func scaleModel(_ entity: ModelEntity) {
            let bounds = entity.visualBounds(relativeTo: nil)
            let size   = bounds.max - bounds.min
            let sx = modelSize.x / max(size.x, 0.001)
            let sy = modelSize.y / max(size.y, 0.001)
            let sz = modelSize.z / max(size.z, 0.001)
            let s  = min(sx, sy, sz)
            entity.scale = M3(repeating: s)
        }

        private func alignBottom(_ entity: ModelEntity, relativeTo reference: Entity) {
            let bounds = entity.visualBounds(relativeTo: reference)
            let bottomY = bounds.min.y
            entity.position.y -= bottomY
        }

        // MARK: Guidance UI

        private func setupGuidanceLabel(in arView: ARView) {
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
                label.bottomAnchor
                    .constraint(equalTo: arView.bottomAnchor, constant: -50),
                label.centerXAnchor
                    .constraint(equalTo: arView.centerXAnchor),
                label.widthAnchor
                    .constraint(lessThanOrEqualTo: arView.widthAnchor, constant: -40)
            ])
            let p = UIEdgeInsets(top: 8, left: 16, bottom: 8, right: 16)
            label.directionalLayoutMargins = NSDirectionalEdgeInsets(
                top: p.top, leading: p.left,
                bottom: p.bottom, trailing: p.right
            )
            guidanceLabel = label
        }

        private func updateGuidanceLabel(text: String) {
            guidanceLabel?.text = text
        }
    }
}
