//
//  Shimmers.swift
//  iosApp
//
//  Created by Глеб Порошин on 13.09.2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI

struct ShimmerView: View {
    @State private var phase: CGFloat = -1

    var body: some View {
        Rectangle()
            .fill(
                LinearGradient(
                    colors: [Color.gray.opacity(0.3), Color.white.opacity(0.6), Color.gray.opacity(0.3)],
                    startPoint: .leading,
                    endPoint: .trailing
                )
            )
            .mask(
                Rectangle()
                    .fill(
                        LinearGradient(
                            colors: [.black.opacity(0.1), .black, .black.opacity(0.1)],
                            startPoint: UnitPoint(x: phase, y: 0.5),
                            endPoint: UnitPoint(x: phase + 0.6, y: 0.5)
                        )
                    )
            )
            .onAppear {
                withAnimation(.easeInOut(duration: 1.6).repeatForever(autoreverses: false)) {
                    phase = 1.2
                }
            }
    }
}

struct ShimmerBlock: View {
    var height: CGFloat
    var width: CGFloat? = nil
    var corner: CGFloat = 8

    var body: some View {
        ShimmerView()
            .frame(width: width, height: height)
            .clipShape(RoundedRectangle(cornerRadius: corner))
    }
}

