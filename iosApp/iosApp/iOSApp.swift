import SwiftUI
import ARApp

@main
struct iOSApp: App {
    @StateObject private var router = AppRouter()
    @StateObject private var badgeHolder = SharedVMHolder<CartBadgeState, CartBadgeEvent, CartBadgeAction, CartBadgeViewModel>(
        viewModel: CartBadgeViewModel(),
        initialState: CartBadgeState(count: 0, text: nil)
    )

    init() {
        let arDemo = ProcessInfo.processInfo.arguments.contains("--ar-demo")
        #if DEBUG
        KoinInitKt.doInitKoin(useMockFallback: true, arDemoBlackBg: arDemo)
        #else
        KoinInitKt.doInitKoin(useMockFallback: false, arDemoBlackBg: arDemo)
        #endif
    }

    var body: some Scene {
        WindowGroup {
            if #available(iOS 16.0, *) {
                TabView(selection: $router.selectedTab) {
                    NavigationStack(path: $router.mainPath) {
                        PlpScreen()
                            .background(Color(uiColor: .secondarySystemBackground))
                            .navigationDestination(for: MainRoute.self) { route in
                                switch route {
                                case .productDetail(let sku):
                                    PdpScreen(sku: sku)
                                }
                            }
                    }
                    .tabItem {
                        Label("Главная", systemImage: "house")
                    }
                    .tag(AppTab.main)

                    NavigationStack(path: $router.cartPath) {
                        CartScreen()
                    }
                    .tabItem {
                        Label("Корзина", systemImage: "cart")
                    }
                    .badge(cartBadgeText)
                    .tag(AppTab.cart)
                }
                .fullScreenCover(item: $router.presentedAr, onDismiss: {
                    router.dismissAr()
                }) { route in
                    ArScreen(
                        filePath: route.filePath,
                        modelWidthMm: route.widthMm,
                        modelHeightMm: route.heightMm,
                        modelDepthMm: route.depthMm,
                        placement: route.placement,
                        cartSku: route.cartSku,
                        cartName: route.cartName,
                        cartPriceText: route.cartPriceText,
                        cartImageUrl: route.cartImageUrl
                    )
                }
                .environmentObject(router)
                .onAppear {
                    badgeHolder.start()
                    badgeHolder.sendEvent(CartBadgeEvent.OnCreate())
                }
                .onDisappear {
                    badgeHolder.stop()
                }
            } else {
                Text("iOS 16+ required")
            }
        }
    }

    private var cartBadgeText: String? {
        return badgeHolder.state.text
    }
}
