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

        loadModel(fromPath: filePath)

        // Только лёгкий апдейт — кламп масштаба после жестов
        sceneUpdate = arView.scene.subscribe(to: SceneEvents.Update.self) { [weak self] _ in
            guard let self, case .placed = self.state, let m = self.model else { return }
            var s = m.scale
            s.x = max(self.minScale, min(s.x, self.maxScale))
            s.y = max(self.minScale, min(s.y, self.maxScale))
            s.z = max(self.minScale, min(s.z, self.maxScale))
            if s != m.scale { m.scale = s }
        }

        updateHUD()
    }

    // MARK: - Input

    func setupTapGesture(on view: ARView) {
        let tap = UITapGestureRecognizer(target: self, action: #selector(handleTap(_:)))
        view.addGestureRecognizer(tap)
    }

    @objc private func handleTap(_ gesture: UITapGestureRecognizer) {
        guard case .readyToPlace = state, let arView else { return }
        let point = gesture.location(in: arView)

        guard let hit = raycastOnFloorPreferred(at: point) else {
            // Нет ещё найденной плоскости в точке — просим поводить камерой
            UIImpactFeedbackGenerator(style: .rigid).impactOccurred()
            hud?.setMessage("Не вижу пола в этой точке.\nПроведите камерой, чтобы распознать плоскость.", visible: true)
            return
        }
        placeModel(using: hit)
    }

    // MARK: - Raycast (тап в любую точку)

    /// Предпочитаем пол; если нет классификации – берём любой горизонтальный хит по существующей геометрии.
    private func raycastOnFloorPreferred(at screenPoint: CGPoint) -> ARRaycastResult? {
        guard let arView else { return nil }

        // 1) Стреляем только по существующей геометрии (самое стабильное)
        let results = arView.raycast(from: screenPoint, allowing: .existingPlaneGeometry, alignment: .horizontal)

        // 1.1) Если среди результатов есть плоскость, классифицированная как пол — берём её
        if let floorHit = results.first(where: { ($0.anchor as? ARPlaneAnchor)?.classification == .floor }) {
            return floorHit
        }

        // 1.2) Иначе берём первый результат по горизонтальной плоскости
        if let anyPlaneHit = results.first {
            return anyPlaneHit
        }

        // 2) Фоллбек: можно разрешить оценочную плоскость (будет менее стабильно)
        // Закомментируй эту часть, если хочешь размещать ТОЛЬКО по найденным плоскостям
        let fallback = arView.raycast(from: screenPoint, allowing: .estimatedPlane, alignment: .horizontal)
        return fallback.first
    }

    // MARK: - Placement

    private func placeModel(using hit: ARRaycastResult) {
        guard let arView, var model else { return }

        // Якорь из результата рейкаста — модель будет «прибита» к плоскости мира
        let a = AnchorEntity(raycastResult: hit)

        // Посадка «на пол»: ставим нижней гранью на плоскость + ε, чтобы не мерцала
        let b = model.visualBounds(relativeTo: nil)
        let bottom = b.center.y - b.extents.y / 2
        model.position.y -= bottom
        model.position.y += 0.001 // 1 мм над плоскостью

        a.addChild(model)
        arView.scene.addAnchor(a)

        // Жесты после постановки (без масштаб-хаоса до этого)
        arView.installGestures([.translation, .rotation, .scale], for: model)

        // Короткая анимация появления
        let original = model.scale
        model.scale = original * 0.9
        var t = model.transform; t.scale = original
        model.move(to: t, relativeTo: nil, duration: 0.12, timingFunction: .easeInOut)
        UIImpactFeedbackGenerator(style: .light).impactOccurred()

        anchor = a
        state = .placed
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
                self.state = .readyToPlace
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
        case .readyToPlace:
            hud?.setMessage("Ткни на пол — туда поставим модель", visible: true)
        case .placed:
            hud?.setMessage("Размещено. Жесты: перемещение/поворот/масштаб", visible: true)
            DispatchQueue.main.asyncAfter(deadline: .now() + 1.2) { [weak self] in
                if case .placed = self?.state { self?.hud?.setMessage("", visible: false) }
            }
        case .modelFailed(let msg):
            hud?.setMessage("Ошибка загрузки модели:\n\(msg)", visible: true)
        }
    }
}

