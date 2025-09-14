//
//  ARPlacementState.swift
//  iosApp
//
//  Created by Глеб Порошин on 14.09.2025.
//  Copyright © 2025 orgName. All rights reserved.
//

enum ARPlacementState: Equatable {
    case modelLoading
    case scanning            // нет валидного хита
    case aiming              // есть валидный хит — ретикл
    case placed              // модель заякорена
    case modelFailed(String) // ошибка загрузки
}
