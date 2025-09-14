//
//  ARCoordinator.swift
//  iosApp
//
//  Created by Глеб Порошин on 14.09.2025.
//  Copyright © 2025 orgName. All rights reserved.
//

//
//  ARCoordinator.swift
//  iosApp
//

import Foundation
import RealityKit
import ARKit
import Combine
import UIKit
import simd

@available(iOS 16.0, *)
final class ARCoordinator: NSObject {
    weak var arView: ARView?
    weak var hud: StatusHUD?

    private var cancellables: Set<AnyCancellable> = []
    private var sceneUpdate: Cancellable?

    // Content
    private var model: ModelEntity?
    private var anchor: AnchorEntity?
    private var reticle = PlacementReticle()

    // State
    private(set) var state: ARPlacementState = .modelLoading { didSet { updateHUD() } }

    // Target size (метры)
    private let targetSizeMeters: CGSize
    private let targetDepthMeters: Double

    // Scale policy
    private let minScale: Float = 0.05
    private let maxScale: Float = 5.0
    private let uniformTolerance: Double = 0.05

    init(targetSizeMeters: CGSize, targetDepthMeters: Double) {
        self.targetSizeMeters = targetSizeMeters
        self.targetDepthMeters = targetDepthMeters
    }

    // MARK: - Lifecycle

    func teardown() {
        sceneUpdate?.cancel(); sceneUpdate = nil
        cancellables.removeAll()
        model = nil
        anchor = nil
        arView?.scene.anchors.removeAll()
    }

    // MARK: - Start

    func start(filePath: String) {
        guard let arView else { return }
        state = .modelLoading

        // Ретикл-якорь (identity)
        let reticleAnchor = AnchorEntity(world: matrix_identity_float4x4)
        reticle.show(false)
        reticleAnchor.addChild(reticle.entity)
        arView.scene.addAnchor(reticleAnchor)

        loadModel(fromPath: filePath)

        // Обновления сцены (raycast + ретикл)
        sceneUpdate = arView.scene.subscribe(to: SceneEvents.Update.self) { [weak self] _ in
            self?.tick()
        }

        updateHUD()
    }

    // MARK: - Input

    func setupTapGesture(on view: ARView) {
        let tap = UITapGestureRecognizer(target: self, action: #selector(handleTap(_:)))
        view.addGestureRecognizer(tap)
    }

    @objc private func handleTap(_ gesture: UITapGestureRecognizer) {
        guard state == .aiming, let arView else { return }
        let point = gesture.location(in: arView)
        if let hit = raycast(at: point) {
            placeModel(at: hit.worldTransform, anchorFrom: hit)
        }
    }

    // MARK: - Per-frame

    private func tick() {
        guard let arView else { return }

        switch state {
        case .modelLoading, .modelFailed:
            return
        case .placed:
            if let m = model {
                var s = m.scale
                s.x = max(minScale, min(s.x, maxScale))
                s.y = max(minScale, min(s.y, maxScale))
                s.z = max(minScale, min(s.z, maxScale))
                if s != m.scale { m.scale = s }
            }
        case .scanning, .aiming:
            let center = CGPoint(x: arView.bounds.midX, y: arView.bounds.midY)
            if let hit = raycast(at: center) {
                reticle.move(to: hit.worldTransform)
                reticle.set(valid: true)
                reticle.show(true)
                if state != .aiming { state = .aiming }
            } else {
                reticle.set(valid: false, animated: state == .aiming)
                reticle.show(false)
                if state != .scanning { state = .scanning }
            }
        }
    }

    // MARK: - Raycast

    private func raycast(at screenPoint: CGPoint) -> ARRaycastResult? {
        guard let arView else { return nil }
        let primary = arView.raycast(from: screenPoint, allowing: .existingPlaneGeometry, alignment: .horizontal)
        let hit = primary.first ?? arView.raycast(from: screenPoint, allowing: .estimatedPlane, alignment: .horizontal).first

        if let h = hit, let plane = h.anchor as? ARPlaneAnchor, plane.classification == .floor {
            return h
        }
        return hit
    }

    // MARK: - Placement

    private func placeModel(at worldTransform: float4x4, anchorFrom hit: ARRaycastResult) {
        guard let arView, var model else { return }

        let a = AnchorEntity(raycastResult: hit)

        let b = model.visualBounds(relativeTo: nil)
        let bottom = b.center.y - b.extents.y / 2
        model.position.y -= bottom
        model.position.y += 0.001 // 1 мм над плоскостью

        a.addChild(model)
        arView.scene.addAnchor(a)

        arView.installGestures([.translation, .rotation], for: model)

        // Анимация "появления" масштабом через move(to:)
        let original = model.scale
        model.scale = original * 0.9
        var t = model.transform
        t.scale = original
        model.move(to: t, relativeTo: nil, duration: 0.12, timingFunction: .easeInOut)
        UIImpactFeedbackGenerator(style: .light).impactOccurred()

        anchor = a
        state = .placed
        reticle.show(false)
    }

    // MARK: - Loading

    private func loadModel(fromPath path: String) {
        let url = (URL(string: path)?.isFileURL == true) ? URL(string: path)! : URL(fileURLWithPath: path)

        Entity.loadModelAsync(contentsOf: url)
            .receive(on: DispatchQueue.main)
            .sink(receiveCompletion: { [weak self] completion in
                if case let .failure(error) = completion {
                    self?.state = .modelFailed(error.localizedDescription)
                }
            }, receiveValue: { [weak self] entity in
                guard let self else { return }
                var m = entity
                m.name = "product-model"
                m.generateCollisionShapes(recursive: true)
                self.applyRealWorldScale(for: &m)
                self.model = m
                self.state = .scanning
            })
            .store(in: &cancellables)
    }

    // MARK: - Scaling (реальные габариты)

    private func applyRealWorldScale(for model: inout ModelEntity, allowNonUniform: Bool = false) {
        let b = model.visualBounds(relativeTo: nil)
        let sx0 = Double(b.extents.x), sy0 = Double(b.extents.y), sz0 = Double(b.extents.z)
        guard sx0 > 0, sy0 > 0, sz0 > 0 else { return }

        let tw = max(0.0, targetSizeMeters.width)
        let th = max(0.0, targetSizeMeters.height)
        let td = max(0.0, targetDepthMeters)
        guard tw > 0 || th > 0 || td > 0 else { return }

        let rx = tw > 0 ? tw / sx0 : 1.0
        let ry = th > 0 ? th / sy0 : 1.0
        let rz = td > 0 ? td / sz0 : 1.0

        let ratios = [rx, ry, rz].filter { $0 > 0 }
        let sUni = Float(ratios.sorted()[ratios.count / 2])
        var uni = SIMD3<Float>(repeating: sUni)
        uni.x = max(minScale, min(uni.x, maxScale))
        uni.y = max(minScale, min(uni.y, maxScale))
        uni.z = max(minScale, min(uni.z, maxScale))

        let ex = abs(Double(uni.x) * sx0 - (tw > 0 ? tw : sx0)) / (tw > 0 ? tw : sx0)
        let ey = abs(Double(uni.y) * sy0 - (th > 0 ? th : sy0)) / (th > 0 ? th : sy0)
        let ez = abs(Double(uni.z) * sz0 - (td > 0 ? td : sz0)) / (td > 0 ? td : sz0)
        let maxErr = max(ex, max(ey, ez))

        if maxErr <= uniformTolerance || !allowNonUniform {
            model.scale = uni
        } else {
            let sx = Float(max(minScale, Float(min(rx, Double(maxScale)))))
            let sy = Float(max(minScale, Float(min(ry, Double(maxScale)))))
            let sz = Float(max(minScale, Float(min(rz, Double(maxScale)))))
            model.scale = SIMD3<Float>(sx, sy, sz)
        }
    }

    // MARK: - HUD

    private func updateHUD() {
        switch state {
        case .modelLoading:
            hud?.setMessage("Загрузка модели…", visible: true)
        case .scanning:
            hud?.setMessage("Наведите камеру на пол\nДвигайте устройство для сканирования", visible: true)
        case .aiming:
            hud?.setMessage("Коснитесь экрана, чтобы поставить", visible: true)
        case .placed:
            hud?.setMessage("Объект размещён. Можно ходить вокруг.\nЖесты: перемещение/поворот", visible: true)
            DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) { [weak self] in
                if case .placed = self?.state { self?.hud?.setMessage("", visible: false) }
            }
        case .modelFailed(let msg):
            hud?.setMessage("Не удалось загрузить модель:\n\(msg)", visible: true)
        }
    }

//
//    // MARK: - Lighting
//
//    func addKeyLight() {
//        guard let view = arView else { return }
//        let light = DirectionalLight()
//        light.light.intensity = 15000
//        light.light.temperature = 6500
//        light.shadow = DirectionalLightComponent.Shadow(maximumDistance: 10)
//
//        let lightAnchor = AnchorEntity(world: .init(translation: [0, 2, 0]))
//        lightAnchor.addChild(light)
//        view.scene.addAnchor(lightAnchor)
//    }
}
