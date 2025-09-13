//
//  PlpScreen.swift
//  iosApp
//
//  Created by Глеб Порошин on 13.09.2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI
import ARApp

struct PlpScreen: View {
    @StateObject private var holder = SharedVMHolder<PlpState, PlpEvent, PlpAction, PlpViewModel>(
        viewModel: PlpViewModel(),
        initialState: PlpState.Loading()
    )

    var body: some View {
        content
            .onAppear {
                holder.start { action in
                    switch action {
                    case let a as PlpAction.OpenPdp:
                        print("Open PDP sku=\(a.sku)")
                    default: break
                    }
                }
                holder.sendEvent(PlpEvent.OnCreate())
            }
            .onDisappear { holder.stop() }
    }

    @ViewBuilder
    private var content: some View {
        switch holder.state {
        case _ as PlpState.Loading:
            PlpLoadingView()
        case let content as PlpState.Content:
            PlpContentView(state: content) { sku in
                holder.sendEvent(PlpEvent.OnProductClick(sku: sku))
            }
        default:
            EmptyView()
        }
    }
}

