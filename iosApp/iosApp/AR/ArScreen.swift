import SwiftUI
import RealityKit
import ARKit
import Combine
import ARApp

@available(iOS 16.0, *)
struct ArScreen: View {
    @Environment(\.dismiss) private var dismiss
    @State private var resetRequested = false
    @State private var showGuidance = true
    @State private var isLoading = true
    @State private var loadError: String?
    @State private var preloadedModel: ModelEntity?
    @State private var preloadCancellable: AnyCancellable?

    @StateObject private var arHolder = SharedVMHolder<ArState, ArEvent, ArAction, ArViewModel>(
        viewModel: ArViewModel(),
        initialState: ArState(
            modelUrl: "",
            isLoading: false,
            isModelLoaded: false,
            error: nil,
            placedObjects: [],
            isSingleMode: true,
            currentScale: 1.0,
            currentRotation: KotlinFloatArray(size: 3)
        )
    )

    @StateObject private var cartHolder = SharedVMHolder<CartQuantityState, CartQuantityEvent, CartQuantityAction, CartQuantityViewModel>(
        viewModel: CartQuantityViewModel(),
        initialState: CartQuantityState.Hidden()
    )

    let filePath: String
    let modelWidthMm: Float
    let modelHeightMm: Float
    let modelDepthMm: Float
    let placement: ArPlacement
    let cartSku: Int64?
    let cartName: String?
    let cartPriceText: String?
    let cartImageUrl: String?

    var body: some View {
        ZStack(alignment: .top) {
            if KoinInitKt.isArDemoBlackBg() {
                Color.black.ignoresSafeArea()
                controls.padding()
            } else if !isLoading, loadError == nil, let preloadedModel {
                ARViewContainer(
                    filePath: filePath,
                    preloadedModel: preloadedModel,
                    placement: placement,
                    modelWidthMm: modelWidthMm,
                    modelHeightMm: modelHeightMm,
                    modelDepthMm: modelDepthMm,
                    onResetRequest: { resetRequested = false },
                    resetRequested: resetRequested,
                    showGuidance: showGuidance,
                    isSingleMode: arHolder.state.isSingleMode,
                    arHolder: arHolder
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
        .onAppear {
            if !KoinInitKt.isArDemoBlackBg() {
                startPreload()
            }

            arHolder.start { action in
                switch action {
                case _ as ArAction.NavigateBack:
                    dismiss()
                default:
                    break
                }
            }
            arHolder.sendEvent(ArEvent.OnCreate())

            cartHolder.start()
            cartHolder.sendEvent(CartQuantityEvent.SetSnapshot(snapshot: makeSnapshot()))
        }
        .onDisappear {
            arHolder.stop()
            cartHolder.stop()
        }
    }

    private var isSingleMode: Bool {
        arHolder.state.isSingleMode
    }

    private var controls: some View {
        VStack(spacing: 12) {
            topControls
            Spacer()
            bottomControls
        }
    }

    private var topControls: some View {
        VStack(spacing: 12) {
            HStack(spacing: 12) {
                chipIconButton(systemImage: "chevron.backward", enabled: true) { dismiss() }
                chipButton(
                    text: isSingleMode ? "Режим: одна" : "Режим: несколько",
                    enabled: true,
                    accessibilityId: "ar_mode_toggle"
                ) {
                    arHolder.sendEvent(ArEvent.SetSingleMode(enabled: !isSingleMode))
                }
                chipButton(
                    text: showGuidance ? "Подсказки вкл" : "Подсказки выкл",
                    enabled: true
                ) {
                    showGuidance.toggle()
                }
                chipButton(
                    text: "Очистить",
                    enabled: !arHolder.state.placedObjects.isEmpty
                ) {
                    arHolder.sendEvent(ArEvent.ClearAll())
                    resetRequested = true
                }
            }

            let placed = arHolder.state.placedObjects.count
            if placed > 0 {
                pill("Объектов в сцене: \(placed)")
            }
        }
    }

    private var bottomControls: some View {
        if let content = cartHolder.state as? CartQuantityState.Content {
            let quantity = Int(content.quantity)
            return AnyView(
                HStack(spacing: 8) {
                    if quantity > 0 {
                        Button("-") { cartHolder.sendEvent(CartQuantityEvent.OnDecrease()) }
                            .buttonStyle(.bordered)
                        pill("\(quantity)")
                        Button("+") { cartHolder.sendEvent(CartQuantityEvent.OnIncrease()) }
                            .buttonStyle(.borderedProminent)
                    } else {
                        Button("Добавить в корзину") {
                            cartHolder.sendEvent(CartQuantityEvent.OnIncrease())
                        }
                        .buttonStyle(.borderedProminent)
                    }
                }
                .padding(.bottom, 96)
            )
        }
        return AnyView(EmptyView())
    }

    @ViewBuilder
    private func chipButton(
        text: String,
        enabled: Bool,
        accessibilityId: String? = nil,
        action: @escaping () -> Void
    ) -> some View {
        let base = Button(action: action) {
            Text(text)
                .font(.subheadline)
                .padding(.horizontal, 12)
                .padding(.vertical, 6)
                .background(
                    Capsule().stroke(
                        enabled ? Color.primary.opacity(0.4) : Color.primary.opacity(0.15),
                        lineWidth: 1
                    )
                )
                .foregroundColor(enabled ? .primary : .secondary)
        }
        .disabled(!enabled)
        if let id = accessibilityId {
            base.accessibilityIdentifier(id)
        } else {
            base
        }
    }

    private func chipIconButton(
        systemImage: String,
        enabled: Bool,
        action: @escaping () -> Void
    ) -> some View {
        Button(action: action) {
            Image(systemName: systemImage)
                .font(.subheadline.weight(.semibold))
                .padding(.horizontal, 12)
                .padding(.vertical, 6)
                .background(
                    Capsule().stroke(
                        enabled ? Color.primary.opacity(0.4) : Color.primary.opacity(0.15),
                        lineWidth: 1
                    )
                )
                .foregroundColor(enabled ? .primary : .secondary)
        }
        .disabled(!enabled)
        .accessibilityLabel("Назад")
    }

    private func pill(_ text: String) -> some View {
        Text(text)
            .font(.caption)
            .foregroundColor(.white)
            .padding(.horizontal, 8)
            .padding(.vertical, 4)
            .background(Color.black.opacity(0.55))
            .clipShape(RoundedRectangle(cornerRadius: 4))
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
                Button(action: { dismiss() }) {
                    Image(systemName: "chevron.backward")
                        .font(.title2)
                        .foregroundColor(.white)
                }
                .padding(.top, 8)
                .accessibilityLabel("Назад")
                .accessibilityIdentifier("ar_error_close_btn")
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

    private func makeSnapshot() -> CartItemSnapshot? {
        guard let sku = cartSku,
              let name = cartName,
              let price = cartPriceText,
              let image = cartImageUrl else {
            return nil
        }

        return CartItemSnapshot(
            sku: sku,
            name: name,
            priceText: price,
            imageUrl: image
        )
    }
}
