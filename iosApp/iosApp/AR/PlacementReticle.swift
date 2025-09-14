//
//  PlacementReticle.swift
//  iosApp
//
//  Created by Глеб Порошин on 14.09.2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import RealityKit
import simd

@available(iOS 16.0, *)
final class PlacementReticle {
    let entity: ModelEntity
    private let validMaterial: SimpleMaterial
    private let invalidMaterial: SimpleMaterial

    init(diameter: Float = 0.20, thickness: Float = 0.002) {
        let mesh = MeshResource.generatePlane(width: diameter, depth: diameter)
        validMaterial = SimpleMaterial(color: .init(white: 1.0, alpha: 0.65), isMetallic: false)
        invalidMaterial = SimpleMaterial(color: .init(red: 1, green: 0.2, blue: 0.2, alpha: 0.65), isMetallic: false)

        entity = ModelEntity(mesh: mesh, materials: [invalidMaterial])
        entity.name = "reticle"
        entity.scale = .init(repeating: 1)
        entity.position = .zero
        entity.orientation = simd_quatf(angle: 0, axis: [0,1,0])
        // Убрано HoverEffectComponent(.lift) — не везде доступно
        entity.position.y = thickness
        set(valid: false, animated: false)
    }

    func set(valid: Bool, animated: Bool = true) {
        let mat = valid ? validMaterial : invalidMaterial
        entity.model?.materials = [mat]
        guard animated else { return }

        let from = entity.scale
        let factor: Float = valid ? 1.05 : 0.98
        let up = SIMD3<Float>(repeating: from.x * factor)

        // пульс масштабом через move(to:)
        var tUp = entity.transform
        tUp.scale = up
        entity.move(to: tUp, relativeTo: nil, duration: 0.08, timingFunction: .easeInOut)

        var tBack = entity.transform
        tBack.scale = from
        entity.move(to: tBack, relativeTo: nil, duration: 0.12, timingFunction: .easeInOut)
    }

    func move(to worldTransform: float4x4) {
        entity.move(to: Transform(matrix: worldTransform), relativeTo: nil, duration: 0.06, timingFunction: .easeInOut)
    }

    func show(_ flag: Bool) {
        entity.isEnabled = flag
    }
}

