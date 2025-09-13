//
//  PlpPairItemsView.swift
//  iosApp
//
//  Created by Глеб Порошин on 13.09.2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI
import ARApp

private struct CardHeightKey: PreferenceKey {
    static var defaultValue: [Int: CGFloat] = [:]
    static func reduce(value: inout [Int: CGFloat], nextValue: () -> [Int : CGFloat]) {
        value.merge(nextValue(), uniquingKeysWith: { max($0, $1) })
    }
}

private enum CardSide: Int { case left = 0, right = 1 }

struct PlpPairItemsView: View {
    let left: Product
    let right: Product?
    let onTap: (Int64) -> Void

    @State private var leftHeight: CGFloat = 0
    @State private var rightHeight: CGFloat = 0

    var body: some View {
        let dividerWidth: CGFloat = 1
        let sideSpacing: CGFloat = 4
        let contentWidth = UIScreen.main.bounds.width - dividerWidth
        let cardWidth = contentWidth / 2 - sideSpacing * 2
        let rowHeight = max(leftHeight, rightHeight)

        VStack(spacing: 0) {
            HStack(alignment: .top, spacing: 0) {
                ProductCardView(product: left, onTap: onTap)
                    .frame(width: cardWidth)
                    .frame(minHeight: rowHeight, alignment: .top)
                
                 Rectangle()
                    .frame(width: dividerWidth)
                    .foregroundColor(Color.secondary.opacity(0.2))
 
                if let right = right {
                    ProductCardView(product: right, onTap: onTap)
                        .frame(width: cardWidth)
                        .frame(minHeight: rowHeight, alignment: .top)
                } else {
                    Rectangle()
                        .foregroundColor(Color.clear)
                        .frame(width: cardWidth)
                        .padding(.vertical, 16)
                }
            }

            Rectangle()
                .frame(height: 1)
                .foregroundColor(Color.secondary.opacity(0.2))
        }
    }
}

struct PlpPairItemsShimmerView: View {
    var body: some View {
        HStack(alignment: .top, spacing: 8) {
            VStack(alignment: .leading, spacing: 10) {
                ShimmerBlock(height: UIScreen.main.bounds.width / 2 - 24)
                    .frame(maxWidth: .infinity)
                    .aspectRatio(1, contentMode: .fit)
                ShimmerBlock(height: 18, width: UIScreen.main.bounds.width * 0.4)
                ShimmerBlock(height: 14, width: UIScreen.main.bounds.width * 0.3, corner: 6)
            }
            .frame(maxWidth: .infinity)

            VStack(alignment: .leading, spacing: 10) {
                ShimmerBlock(height: UIScreen.main.bounds.width / 2 - 24)
                    .frame(maxWidth: .infinity)
                    .aspectRatio(1, contentMode: .fit)
                ShimmerBlock(height: 18, width: UIScreen.main.bounds.width * 0.4)
                ShimmerBlock(height: 14, width: UIScreen.main.bounds.width * 0.3, corner: 6)
            }
            .frame(maxWidth: .infinity)
        }
        .padding(.horizontal, 8)
        .padding(.vertical, 6)
    }
}

struct PlpContentView: View {
    let state: PlpState.Content
    let onProductClick: (Int64) -> Void

    var body: some View {
        ScrollView {
            LazyVStack(spacing: 0) {
                ForEach(Array(state.items.chunked(into: 2).enumerated()), id: \.offset) { _, pair in
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
            let columns = [GridItem(.flexible(), spacing: 8), GridItem(.flexible(), spacing: 8)]
            LazyVGrid(columns: columns, spacing: 12) {
                ForEach(0..<6, id: \.self) { _ in
                    VStack(alignment: .leading, spacing: 10) {
                        ShimmerBlock(height: UIScreen.main.bounds.width / 2 - 24)
                            .frame(maxWidth: .infinity)
                            .aspectRatio(1, contentMode: .fit)
                        ShimmerBlock(height: 18, width: UIScreen.main.bounds.width * 0.4)
                        ShimmerBlock(height: 14, width: UIScreen.main.bounds.width * 0.3, corner: 6)
                    }
                }
            }
            .padding(.horizontal, 8)
            .padding(.vertical, 6)
        }
    }
}

private extension Array {
    func chunked(into size: Int) -> [[Element]] {
        stride(from: 0, to: count, by: size).map { Array(self[$0..<Swift.min($0 + size, count)]) }
    }
}

