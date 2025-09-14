//
//  ARPlacementState.swift
//  iosApp
//
//  Created by Глеб Порошин on 14.09.2025.
//  Copyright © 2025 orgName. All rights reserved.
//

enum ARPlacementState: Equatable {
    case modelLoading
    case readyToPlace        // модель загружена, ждем тапа
    case placed              // модель размещена и заякорена
    case modelFailed(String) // ошибка загрузки
}
