//
//  ArScreen.swift
//  iosApp
//
//  Created by Глеб Порошин on 14.09.2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI
import RealityKit
import ARKit
import Combine

@available(iOS 16.0, *)
struct ArScreen: View {
    @Environment(\.dismiss) private var dismiss
    @State private var resetRequested = false
    @State private var showGuidance = true
    @State private var isLoading = true
    @State private var loadError: String?
    @State private var preloadedModel: ModelEntity?
    @State private var preloadCancellable: AnyCancellable?

    let filePath: String
    let modelWidthMm:  Float
    let modelHeightMm: Float
    let modelDepthMm:  Float

    var body: some View {
        ZStack(alignment: .top) {
            if !isLoading, loadError == nil, let preloadedModel {
                ARViewContainer(
                    filePath: filePath,
                    preloadedModel: preloadedModel,
                    modelWidthMm: modelWidthMm,
                    modelHeightMm: modelHeightMm,
                    modelDepthMm: modelDepthMm,
                    onResetRequest:{ resetRequested = false },
                    resetRequested: resetRequested,
                    showGuidance: showGuidance
                )
                .ignoresSafeArea()

                controls
                    .padding()
            } else if isLoading {
                loadingView
                    .ignoresSafeArea()
            } else if let loadError {
                errorView(loadError)
                    .ignoresSafeArea()
            }
        }
        .onAppear { startPreload() }
    }

    private var controls: some View {
        HStack {
            Button { dismiss() } label: {
                Image(systemName: "xmark.circle.fill")
                    .font(.system(size: 36))
                    .foregroundColor(.white)
                    .padding()
                    .background(Color.black.opacity(0.3))
                    .clipShape(Circle())
            }

            Spacer()

            Button { showGuidance.toggle() } label: {
                Image(systemName: showGuidance ? "eye.slash.circle.fill" : "eye.circle.fill")
                    .font(.system(size: 36))
                    .foregroundColor(.white)
                    .padding()
                    .background(Color.black.opacity(0.3))
                    .clipShape(Circle())
            }

            Button { resetRequested = true } label: {
                Image(systemName: "arrow.triangle.2.circlepath.circle.fill")
                    .font(.system(size: 36))
                    .foregroundColor(.white)
                    .padding()
                    .background(Color.black.opacity(0.3))
                    .clipShape(Circle())
            }
        }
    }

    private var loadingView: some View {
        ZStack {
            Color.black.opacity(0.9)
            VStack(spacing: 16) {
                ProgressView("Загрузка модели…")
                    .progressViewStyle(.circular)
                    .tint(.white)
                Text("Подождите, идёт подготовка AR")
                    .foregroundColor(.white)
            }
        }
    }

    private func errorView(_ message: String) -> some View {
        ZStack {
            Color.black.opacity(0.9)
            VStack(spacing: 16) {
                Image(systemName: "exclamationmark.triangle.fill")
                    .foregroundColor(.yellow)
                    .font(.system(size: 44))
                Text(message)
                    .foregroundColor(.white)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 24)
                Button("Закрыть") { dismiss() }
                    .padding(.top, 8)
            }
            .padding()
        }
    }

    private func startPreload() {
        guard preloadedModel == nil else {
            isLoading = false
            return
        }
        isLoading = true
        loadError = nil
        let url = resolveURL(from: filePath)
        preloadCancellable = Entity.loadModelAsync(contentsOf: url)
            .receive(on: DispatchQueue.main)
            .sink(receiveCompletion: { completion in
                switch completion {
                case .finished:
                    isLoading = false
                case .failure(let err):
                    loadError = err.localizedDescription
                    isLoading = false
                }
            }, receiveValue: { entity in
                preloadedModel = entity
            })
    }

    private func resolveURL(from path: String) -> URL {
        if path.hasPrefix("/") {
            return URL(fileURLWithPath: path)
        }
        let cachesURL = FileManager.default.urls(for: .cachesDirectory, in: .userDomainMask).first!
        return cachesURL.appendingPathComponent(path)
    }
}

