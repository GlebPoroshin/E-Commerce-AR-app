//
//  PdpScreen.swift
//  iosApp
//
//  Created by Глеб Порошин on 13.09.2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI
import ARApp
import Foundation

@available(iOS 16.0, *)
struct PdpScreen: View {
    let sku: Int64
    @StateObject private var holder = SharedVMHolder<PdpState, PdpEvent, PdpAction, PdpViewModel>(
        viewModel: PdpViewModel(),
        initialState: PdpState.Loading()
    )

    @EnvironmentObject private var router: AppRouter

    var body: some View {
        content
            .onAppear {
                holder.start { action in
                    switch action {
                    case let a as PdpAction.OpenArObject:
                        let pathString = a.filePath.description
                        router.presentAr(
                            filePath: pathString,
                            widthMm: a.width,
                            heightMm: a.height,
                            depthMm: a.depth,
                            placement: a.placement,
                            cartSku: a.cartSnapshot?.sku,
                            cartName: a.cartSnapshot?.name,
                            cartPriceText: a.cartSnapshot?.priceText,
                            cartImageUrl: a.cartSnapshot?.imageUrl
                        )
                    default: break
                    }
                }
                holder.sendEvent(PdpEvent.OnCreate(sku: sku))
            }
            .onDisappear { holder.stop() }
    }

    @ViewBuilder
    private var content: some View {
        switch holder.state {
        case _ as PdpState.Loading:
            ProgressView().padding()

        case let content as PdpState.Content:
            VStack(alignment: .leading, spacing: 16) {
                Text("Product Detail").font(.largeTitle).bold()
                Text("SKU: \(content.product.sku)").font(.headline)
                if let name = content.product.name as String? { Text(name) }
                if let price = content.product.price as String? { Text("Price: \(price)") }

                if let percent = content.loadingState?.intValue {
                    VStack(alignment: .leading, spacing: 8) {
                        ProgressView(value: Double(percent) / 100.0)
                        Text("Loading: \(percent)%")
                            .font(.subheadline)
                            .foregroundStyle(.secondary)
                    }
                }

                if content.isModelExists {
                    HStack(spacing: 12) {
                        Button("Посмотреть в AR") {
                            holder.sendEvent(
                                PdpEvent.OnModelLoad(state: content)
                            )
                        }
                        .buttonStyle(.borderedProminent)

                        Button("Удалить модель") {
                            holder.sendEvent(
                                PdpEvent.OnDeleteModel(sku: content.product.sku)
                            )
                        }
                        .buttonStyle(.bordered)
                        .tint(.red)
                    }
                } else {
                    Button("Скачать модель") {
                        holder.sendEvent(
                            PdpEvent.OnModelLoad(state: content)
                        )
                    }
                    .buttonStyle(.borderedProminent)
                }

                let quantity = Int(content.cartQuantity)
                if quantity > 0 {
                    HStack(spacing: 16) {
                        Button("<") {
                            holder.sendEvent(
                                PdpEvent.OnDecreaseCart(sku: content.product.sku)
                            )
                        }
                        .buttonStyle(.bordered)

                        Text("\(quantity)")
                            .font(.title3)
                            .bold()

                        Button(">") {
                            holder.sendEvent(
                                PdpEvent.OnIncreaseCart(snapshot: makeSnapshot(from: content))
                            )
                        }
                        .buttonStyle(.borderedProminent)
                    }
                } else {
                    Button("Добавить в корзину") {
                        holder.sendEvent(
                            PdpEvent.OnIncreaseCart(snapshot: makeSnapshot(from: content))
                        )
                    }
                    .buttonStyle(.borderedProminent)
                }

                Spacer()
            }
            .padding()
            .navigationTitle("PDP")
            .navigationBarTitleDisplayMode(.inline)

        default:
            EmptyView()
        }
    }

    private func makeSnapshot(from content: PdpState.Content) -> CartItemSnapshot {
        let image: String = {
            if let typed = content.product.images as? [String], let first = typed.first {
                return first
            }
            if let nsArray = content.product.images as? NSArray {
                return nsArray.compactMap { $0 as? String }.first ?? ""
            }
            return ""
        }()
        return CartItemSnapshot(
            sku: content.product.sku,
            name: content.product.name,
            priceText: content.product.price,
            imageUrl: image
        )
    }
}
