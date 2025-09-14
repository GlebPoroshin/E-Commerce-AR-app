import SwiftUI
import ARApp

@main
struct iOSApp: App {
    @StateObject private var router = AppRouter()
    init() {
        KoinInitKt.doInitKoin()
    }
	var body: some Scene {
		WindowGroup {
			if #available(iOS 16.0, *) {
				NavigationStack(path: $router.path) {
					PlpScreen()
                        .background(Color(uiColor: .secondarySystemBackground))
						.navigationDestination(for: AppRoute.self) { route in
							switch route {
							case .productDetail(let sku):
								PdpScreen(sku: sku)
							case .arObject(let filePath, let widthMm, let heightMm, let depthMm):
								ArScreen(
									filePath: filePath,
									modelWidthMm: widthMm,
                                    modelHeightMm: heightMm,
                                    modelDepthMm: depthMm
								)
							}
						}
				}
				.environmentObject(router)
			} else {
				Text("iOS 16+ required")
			}
		}
	}
}
