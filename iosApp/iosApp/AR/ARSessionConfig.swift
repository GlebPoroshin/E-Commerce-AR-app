//
//  ARSessionConfig.swift
//  iosApp
//
//  Created by Глеб Порошин on 14.09.2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import RealityKit
import ARKit

@available(iOS 16.0, *)
func configureARSession(for arView: ARView) {
    let config = ARWorldTrackingConfiguration()
    config.planeDetection = [.horizontal]
    config.environmentTexturing = .automatic
    config.isAutoFocusEnabled = true

    if ARWorldTrackingConfiguration.supportsFrameSemantics(.sceneDepth) {
        config.frameSemantics.insert(.sceneDepth)
    }
    if ARWorldTrackingConfiguration.supportsFrameSemantics(.personSegmentationWithDepth) {
        config.frameSemantics.insert(.personSegmentationWithDepth)
    }
    if ARWorldTrackingConfiguration.supportsSceneReconstruction(.mesh) {
        config.sceneReconstruction = .mesh
    }

    arView.session.run(config, options: [.resetTracking, .removeExistingAnchors])

    // Реализм окружения (можно отключить occlusion, если модель "пропадает" за шумной глубиной)
    arView.environment.sceneUnderstanding.options.insert([.collision, .occlusion, .receivesLighting])
    // Отладка при необходимости:
     arView.debugOptions = [.showSceneUnderstanding, .showPhysics]
}
