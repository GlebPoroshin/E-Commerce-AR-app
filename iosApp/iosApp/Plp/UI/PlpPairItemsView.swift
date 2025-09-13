//
//  PlpPairItemsView.swift
//  iosApp
//
//  Created by Глеб Порошин on 13.09.2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI
import ARApp

struct PlpPairItemsView: View {
    let left: Product
    let right: Product?
    let onTap: (Int64) -> Void

    var body: some View {
        VStack(spacing: 0) {
            HStack(alignment: .center, spacing: 0) {
                ProductCardView(product: left, onTap: onTap)
                    .frame(maxWidth: .infinity)
                    .padding(4)

                if let right = right {
                    Divider()
                        .frame(width: 2)
                        .background(Color.secondary.opacity(0.3))
                    ProductCardView(product: right, onTap: onTap)
                        .frame(maxWidth: .infinity)
                        .padding(4)
                }
            }
            Divider()
                .frame(height: 2)
                .background(Color.secondary.opacity(0.3))
        }
    }
}

struct PlpPairItemsShimmerView: View {
    var body: some View {
        VStack(spacing: 0) {
            HStack(alignment: .center, spacing: 0) {
                VStack(alignment: .leading, spacing: 10) {
                    ShimmerBlock(height: UIScreen.main.bounds.width / 2 - 8)
                        .frame(maxWidth: .infinity)
                        .aspectRatio(1, contentMode: .fit)
                    ShimmerBlock(height: 18, width: UIScreen.main.bounds.width * 0.4)
                    ShimmerBlock(height: 14, width: UIScreen.main.bounds.width * 0.3, corner: 6)
                }
                .frame(maxWidth: .infinity)
                .padding(4)

                Divider()
                    .frame(width: 2)
                    .background(Color.secondary.opacity(0.3))

                VStack(alignment: .leading, spacing: 10) {
                    ShimmerBlock(height: UIScreen.main.bounds.width / 2 - 8)
                        .frame(maxWidth: .infinity)
                        .aspectRatio(1, contentMode: .fit)
                    ShimmerBlock(height: 18, width: UIScreen.main.bounds.width * 0.4)
                    ShimmerBlock(height: 14, width: UIScreen.main.bounds.width * 0.3, corner: 6)
                }
                .frame(maxWidth: .infinity)
                .padding(4)
            }
            Divider()
                .frame(height: 2)
                .background(Color.secondary.opacity(0.3))
        }
    }
}

struct PlpContentView: View {
    let state: PlpState.Content
    let onProductClick: (Int64) -> Void

    var body: some View {
        ScrollView {
            LazyVStack(spacing: 0) {
                ForEach(Array(state.items.chunked(into: 2).enumerated()), id: \ .offset) { _, pair in
                    let left = pair.first!
                    let right = pair.count > 1 ? pair[1] : nil
                    PlpPairItemsView(left: left, right: right, onTap: onProductClick)
                }
            }
        }
    }
}

struct PlpLoadingView: View {
    var body: some View {
        ScrollView {
            LazyVStack(spacing: 0) {
                ForEach(0..<3, id: \.self) { _ in
                    PlpPairItemsShimmerView()
                }
            }
        }
    }
}

private extension Array {
    func chunked(into size: Int) -> [[Element]] {
        stride(from: 0, to: count, by: size).map { Array(self[$0..<Swift.min($0 + size, count)]) }
    }
}

