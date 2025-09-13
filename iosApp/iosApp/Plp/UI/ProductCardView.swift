//
//  ProductCardView.swift
//  iosApp
//
//  Created by Глеб Порошин on 13.09.2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI
import ARApp

struct ProductCardView: View {
    let product: Product
    let onTap: (Int64) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            AsyncImage(url: URL(string: product.imageUrl)) { phase in
                switch phase {
                case .success(let image):
                    image
                        .resizable()
                        .scaledToFill()
                default:
                    Color.gray.opacity(0.2)
                }
            }
            .frame(maxWidth: .infinity)
            .aspectRatio(1, contentMode: .fit)
            .clipped()

            VStack(alignment: .leading, spacing: 6) {
                Text(product.name)
                    .font(.headline)
                    .lineLimit(2)

                VStack(alignment: .leading, spacing: 2) {
                    Text("\(product.price) ₽")
                        .font(.headline).bold()
                    if let old = product.oldPrice {
                        Text("\(old) ₽")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                            .strikethrough()
                    }
                }

                if product.rate > 0 {
                    Text("★ \(String(format: "%.1f", product.rate))")
                        .font(.subheadline)
                        .foregroundColor(.accentColor)
                }
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 10)
        }
        .contentShape(Rectangle())
        .onTapGesture {
            onTap(product.sku)
        }
    }
}

