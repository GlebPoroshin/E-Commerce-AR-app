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

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Product Detail")
                .font(.largeTitle).bold()
            Text("SKU: \(sku)")
                .font(.headline)
            Spacer()
        }
        .padding()
        .navigationTitle("PDP")
        .navigationBarTitleDisplayMode(.inline)
    }
}
