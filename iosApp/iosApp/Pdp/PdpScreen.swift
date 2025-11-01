//
//  PdpScreen.swift
//  iosApp
//
//  Created by Глеб Порошин on 13.09.2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI
import ARApp

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
                        router.push(
                            .arObject(
                                filePath: pathString,
                                widthMm: a.width,
                                heightMm: a.height,
                                depthMm: a.depth,
                                placement: a.placement
                            )
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

                Spacer()
            }
            .padding()
            .navigationTitle("PDP")
            .navigationBarTitleDisplayMode(.inline)

        default:
            EmptyView()
        }
    }
}
