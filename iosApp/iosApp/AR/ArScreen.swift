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
            if !isLoading, loadError == nil, let preloadedModel {
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
            startPreload()

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

                Button {
                    let newMode = !isSingleMode
                    arHolder.sendEvent(ArEvent.SetSingleMode(enabled: newMode))
                } label: {
                    Text(isSingleMode ? "1" : "N")
                        .font(.system(size: 20, weight: .bold))
                        .foregroundColor(.white)
                        .frame(width: 44, height: 44)
                        .background(Color.black.opacity(0.3))
                        .clipShape(Circle())
                }
                .accessibilityIdentifier("ar_mode_toggle")

                Button {
                    arHolder.sendEvent(ArEvent.ClearAll())
                    resetRequested = true
                } label: {
                    Image(systemName: "arrow.triangle.2.circlepath.circle.fill")
                        .font(.system(size: 36))
                        .foregroundColor(.white)
                        .padding()
                        .background(Color.black.opacity(0.3))
                        .clipShape(Circle())
                }
            }

            if let content = cartHolder.state as? CartQuantityState.Content {
                let quantity = Int(content.quantity)
                HStack(spacing: 12) {
                    if quantity > 0 {
                        Button("-") {
                            cartHolder.sendEvent(CartQuantityEvent.OnDecrease())
                        }
                        .buttonStyle(.bordered)

                        Text("\(quantity)")
                            .font(.headline)
                            .foregroundStyle(.white)
                            .padding(.horizontal, 8)

                        Button("+") {
                            cartHolder.sendEvent(CartQuantityEvent.OnIncrease())
                        }
                        .buttonStyle(.borderedProminent)
                    } else {
                        Button("Добавить в корзину") {
                            cartHolder.sendEvent(CartQuantityEvent.OnIncrease())
                        }
                        .buttonStyle(.borderedProminent)
                    }
                }
                .padding(8)
                .background(Color.black.opacity(0.35))
                .clipShape(RoundedRectangle(cornerRadius: 12))
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
