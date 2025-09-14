//
//  ARSessionConfig.swift
//  iosApp
//
//  Created by Глеб Порошин on 14.09.2025.
//  Copyright © 2025 orgName. All rights reserved.
//

//
//  ARSessionConfig.swift
//  iosApp
//

import RealityKit
import ARKit

@available(iOS 16.0, *)
func configureARSession(for arView: ARView) {
    let config = ARWorldTrackingConfiguration()
    config.planeDetection = [.horizontal]          // нам нужен пол
    config.environmentTexturing = .automatic
    config.isAutoFocusEnabled = true

    if ARWorldTrackingConfiguration.supportsFrameSemantics(.sceneDepth) {
        config.frameSemantics.insert(.sceneDepth)
    }
    if ARWorldTrackingConfiguration.supportsSceneReconstruction(.mesh) {
        config.sceneReconstruction = .mesh
    }

    arView.session.run(config, options: [.resetTracking, .removeExistingAnchors])

    // Окклюзия можно включить/выключить в зависимости от устройства/шума глубины:
    arView.environment.sceneUnderstanding.options.insert([.collision])
    // Если хочешь окклюзию — раскомментируй:
    // arView.environment.sceneUnderstanding.options.insert(.occlusion)

    // Для отладки:
     arView.debugOptions = [.showAnchorGeometry, .showSceneUnderstanding, .showStatistics]
}
