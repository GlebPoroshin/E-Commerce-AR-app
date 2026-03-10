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
                    default:
                        break
                    }
                }
                holder.sendEvent(PdpEvent.OnCreate(sku: sku))
            }
            .onChange(of: router.presentedAr) { route in
                if route == nil {
                    holder.sendEvent(PdpEvent.OnResume())
                }
            }
            .onDisappear { holder.stop() }
    }

    @ViewBuilder
    private var content: some View {
        switch holder.state {
        case _ as PdpState.Loading:
            ProgressView().padding()

        case let content as PdpState.Content:
            ScrollView {
                VStack(alignment: .leading, spacing: 0) {
                    PdpHeaderImage(urlString: firstImageUrl(from: content))

                    VStack(alignment: .leading, spacing: 16) {
                        Text(content.product.name)
                            .font(.title2)
                            .fontWeight(.semibold)

                        Text("SKU \(content.product.sku)")
                            .font(.subheadline)
                            .foregroundStyle(.secondary)

                        Text(content.product.price)
                            .font(.title3)
                            .fontWeight(.bold)

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
                                    holder.sendEvent(PdpEvent.OnModelLoad(state: content))
                                }
                                .buttonStyle(.borderedProminent)
                                .frame(maxWidth: .infinity)

                                Button("Удалить модель") {
                                    holder.sendEvent(PdpEvent.OnDeleteModel(sku: content.product.sku))
                                }
                                .buttonStyle(.bordered)
                                .frame(maxWidth: .infinity)
                            }
                        } else {
                            Button("Скачать модель") {
                                holder.sendEvent(PdpEvent.OnModelLoad(state: content))
                            }
                            .buttonStyle(.borderedProminent)
                            .frame(maxWidth: .infinity)
                        }

                        let quantity = Int(content.cartQuantity)
                        if quantity > 0 {
                            HStack(spacing: 12) {
                                Button("-") {
                                    holder.sendEvent(PdpEvent.OnDecreaseCart(sku: content.product.sku))
                                }
                                .buttonStyle(.bordered)
                                .frame(maxWidth: .infinity)

                                Text("\(quantity)")
                                    .font(.title3)
                                    .fontWeight(.bold)

                                Button("+") {
                                    holder.sendEvent(PdpEvent.OnIncreaseCart(snapshot: makeSnapshot(from: content)))
                                }
                                .buttonStyle(.bordered)
                                .frame(maxWidth: .infinity)
                            }
                        } else {
                            Button("Добавить в корзину") {
                                holder.sendEvent(PdpEvent.OnIncreaseCart(snapshot: makeSnapshot(from: content)))
                            }
                            .buttonStyle(.borderedProminent)
                            .frame(maxWidth: .infinity)
                        }
                    }
                    .padding(.horizontal, 20)
                    .padding(.vertical, 16)
                }
            }
            .navigationTitle("PDP")
            .navigationBarTitleDisplayMode(.inline)

        default:
            EmptyView()
        }
    }

    private func firstImageUrl(from content: PdpState.Content) -> String? {
        return content.product.images.first
    }

    private func makeSnapshot(from content: PdpState.Content) -> CartItemSnapshot {
        CartItemSnapshot(
            sku: content.product.sku,
            name: content.product.name,
            priceText: content.product.price,
            imageUrl: firstImageUrl(from: content) ?? ""
        )
    }
}

@available(iOS 16.0, *)
private struct PdpHeaderImage: View {
    let urlString: String?

    var body: some View {
        Group {
            if let urlString, let url = URL(string: urlString) {
                AsyncImage(url: url) { phase in
                    switch phase {
                    case .success(let image):
                        image
                            .resizable()
                            .scaledToFit()
                            .frame(maxWidth: .infinity)
                    case .failure:
                        placeholder(text: "Ошибка загрузки изображения")
                    case .empty:
                        ZStack {
                            placeholder(text: nil)
                            ProgressView()
                        }
                    @unknown default:
                        placeholder(text: "Нет изображения")
                    }
                }
            } else {
                placeholder(text: "Нет изображения")
            }
        }
        .frame(maxWidth: .infinity)
    }

    @ViewBuilder
    private func placeholder(text: String?) -> some View {
        ZStack {
            Color(uiColor: .secondarySystemBackground)
            if let text {
                Text(text)
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
            }
        }
        .frame(maxWidth: .infinity)
        .aspectRatio(4.0 / 3.0, contentMode: .fit)
    }
}
