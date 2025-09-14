//
//  ArScreen.swift
//  iosApp
//
//  Created by Глеб Порошин on 14.09.2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI

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
