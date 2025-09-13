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
    @State private var isDownloaded: Bool = false

    var body: some View {
        content
            .onAppear {
                holder.start { action in
                    switch action {
                    case _ as PdpAction.OpenArViewer:
                        isDownloaded = true
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

                if isDownloaded {
                    Text("Модель скачана")
                        .font(.headline)
                        .foregroundStyle(.green)
                } else {
                    Button("Скачать модель") {
                        let url = content.product.ar?.arRecourceUrl ?? ""
                        let version = content.product.ar?.version?.intValue ?? 0
                        holder.sendEvent(
                            PdpEvent.OnModelLoad(
                                sku: content.product.sku,
                                url: url,
                                version: Int32(version)
                            )
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
