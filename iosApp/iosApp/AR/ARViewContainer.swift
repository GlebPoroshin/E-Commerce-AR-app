//
//  ARViewContainer.swift
//  iosApp
//
//  Created by Глеб Порошин on 14.09.2025.
//  Copyright © 2025 orgName. All rights reserved.
//

//
//  ARViewContainer.swift
//  iosApp
//

import SwiftUI
import RealityKit
import ARKit

@available(iOS 16.0, *)
struct ARViewContainer: UIViewRepresentable {
    typealias Coordinator = ARCoordinator

    let filePath: String
    let widthMm: Int
    let heightMm: Int
    let depthMm: Int

    func makeCoordinator() -> Coordinator {
        ARCoordinator(
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
        configureARSession(for: arView)

        // HUD
        let hud = StatusHUD()
        arView.addSubview(hud)
        NSLayoutConstraint.activate([
            hud.centerXAnchor.constraint(equalTo: arView.centerXAnchor),
            hud.bottomAnchor.constraint(equalTo: arView.bottomAnchor, constant: -40)
        ])

        context.coordinator.hud = hud
        context.coordinator.arView = arView
        context.coordinator.setupTapGesture(on: arView)
        context.coordinator.start(filePath: filePath)

        return arView
    }

    func updateUIView(_ uiView: ARView, context: Context) {}

    static func dismantleUIView(_ uiView: ARView, coordinator: Coordinator) {
        uiView.session.pause()
        coordinator.teardown()
    }
}
