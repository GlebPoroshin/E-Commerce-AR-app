//
//  ArScreen.swift
//  iosApp
//
//  Created by Глеб Порошин on 14.09.2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI
import RealityKit
import ARKit
import Combine

@available(iOS 16.0, *)
struct ArScreen: View {
    let filePath: String
    let widthMm: Int
    let heightMm: Int
    let depthMm: Int

    var body: some View {
        ZStack {
            ARViewContainer(
                filePath: filePath,
                widthMm: widthMm,
                heightMm: heightMm,
                depthMm: depthMm
            )
            .ignoresSafeArea()
        }
        .navigationBarTitleDisplayMode(.inline)
    }
}

@available(iOS 16.0, *)
private struct ARViewContainer: UIViewRepresentable {
    let filePath: String
    let widthMm: Int
    let heightMm: Int
    let depthMm: Int

    func makeCoordinator() -> Coordinator {
        Coordinator(
            targetSizeMeters: CGSize(
                width: max(0.001, Double(widthMm)) / 1000.0,
                height: max(0.001, Double(heightMm)) / 1000.0
            ),
            targetDepthMeters: max(0.001, Double(depthMm)) / 1000.0
        )
    }

    func makeUIView(context: Context) -> ARView {
        let arView = ARView(frame: .zero)
        arView.automaticallyConfigureSession = false

        let configuration = ARWorldTrackingConfiguration()
        configuration.planeDetection = [.horizontal]
        configuration.environmentTexturing = .automatic
        
        if ARWorldTrackingConfiguration.supportsFrameSemantics(.sceneDepth) {
            configuration.frameSemantics.insert(.sceneDepth)
        }
        
        arView.session.run(configuration, options: [.resetTracking, .removeExistingAnchors])

        context.coordinator.arView = arView

        context.coordinator.loadModel(fromPath: filePath)
        context.coordinator.startPlacementLoop()

        return arView
    }

    func updateUIView(_ uiView: ARView, context: Context) {}

    final class Coordinator: NSObject {
        weak var arView: ARView?
        private var cancellables: Set<AnyCancellable> = []
        private var model: ModelEntity?
        private var anchor: AnchorEntity?
        private var isPlaced: Bool = false

        private let targetSizeMeters: CGSize
        private let targetDepthMeters: Double

        init(targetSizeMeters: CGSize, targetDepthMeters: Double) {
            self.targetSizeMeters = targetSizeMeters
            self.targetDepthMeters = targetDepthMeters
        }

        func loadModel(fromPath path: String) {
            let url = URL(fileURLWithPath: path)

            Entity.loadModelAsync(contentsOf: url)
                .receive(on: DispatchQueue.main)
                .sink(receiveCompletion: { [weak self] completion in
                    if case .failure = completion {
                        self?.isPlaced = true
                    }
                }, receiveValue: { [weak self] entity in
                    guard let self, let arView = self.arView else { return }
                    var model = entity
                    model.generateCollisionShapes(recursive: true)

                    self.applyUniformScale(for: &model)

                    arView.installGestures([.translation, .rotation, .scale], for: model)

                    self.model = model
                })
                .store(in: &cancellables)
        }

        private func applyUniformScale(for model: inout ModelEntity) {
            let bounds = model.visualBounds(relativeTo: nil)
            let size = bounds.extents

            let targetMax = max(targetSizeMeters.width, max(targetSizeMeters.height, targetDepthMeters))
            guard targetMax > 0 else { return }

            let modelMax = max(size.x, max(size.y, size.z))
            guard modelMax > 0 else { return }

            let scale = Float(targetMax / Double(modelMax))
            model.scale = SIMD3<Float>(repeating: scale)
        }

        func startPlacementLoop() {
            Timer.scheduledTimer(withTimeInterval: 0.25, repeats: true) { [weak self] timer in
                guard let self else { timer.invalidate(); return }
                if self.isPlaced { timer.invalidate(); return }
                self.tryPlaceAtCenter()
            }
        }

        private func tryPlaceAtCenter() {
            guard let arView else { return }
            guard let model else { return }

            let center = CGPoint(x: arView.bounds.midX, y: arView.bounds.midY)
            let results = arView.raycast(from: center, allowing: .estimatedPlane, alignment: .horizontal)
            guard let first = results.first else { return }

            let anchor = AnchorEntity(world: first.worldTransform)

            let bounds = model.visualBounds(relativeTo: nil)
            let baseOffset = bounds.center.y - bounds.extents.y / 2.0
            model.position.y -= baseOffset

            anchor.addChild(model)
            arView.scene.addAnchor(anchor)

            self.anchor = anchor
            self.isPlaced = true
        }
    }
}

