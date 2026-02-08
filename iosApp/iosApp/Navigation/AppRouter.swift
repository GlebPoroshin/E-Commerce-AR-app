import SwiftUI
import ARApp
import Foundation

enum AppTab: Hashable {
    case main
    case cart
}

enum MainRoute: Hashable {
    case productDetail(sku: Int64)
}

enum CartRoute: Hashable {
    case root
}

struct ArRoute: Identifiable, Hashable {
    let id: String
    let filePath: String
    let widthMm: Float
    let heightMm: Float
    let depthMm: Float
    let placement: ArPlacement
    let cartSku: Int64?
    let cartName: String?
    let cartPriceText: String?
    let cartImageUrl: String?

    init(
        filePath: String,
        widthMm: Float,
        heightMm: Float,
        depthMm: Float,
        placement: ArPlacement,
        cartSku: Int64?,
        cartName: String?,
        cartPriceText: String?,
        cartImageUrl: String?
    ) {
        self.id = UUID().uuidString
        self.filePath = filePath
        self.widthMm = widthMm
        self.heightMm = heightMm
        self.depthMm = depthMm
        self.placement = placement
        self.cartSku = cartSku
        self.cartName = cartName
        self.cartPriceText = cartPriceText
        self.cartImageUrl = cartImageUrl
    }
}

final class AppRouter: ObservableObject {
    @Published var selectedTab: AppTab = .main
    @Published var mainPath = NavigationPath()
    @Published var cartPath = NavigationPath()
    @Published var presentedAr: ArRoute?

    func pushMainProduct(sku: Int64) {
        mainPath.append(MainRoute.productDetail(sku: sku))
    }

    func openProductFromCart(sku: Int64) {
        selectedTab = .main
        pushMainProduct(sku: sku)
    }

    func showMainTab() {
        selectedTab = .main
    }

    func presentAr(
        filePath: String,
        widthMm: Float,
        heightMm: Float,
        depthMm: Float,
        placement: ArPlacement,
        cartSku: Int64?,
        cartName: String?,
        cartPriceText: String?,
        cartImageUrl: String?
    ) {
        presentedAr = ArRoute(
            filePath: filePath,
            widthMm: widthMm,
            heightMm: heightMm,
            depthMm: depthMm,
            placement: placement,
            cartSku: cartSku,
            cartName: cartName,
            cartPriceText: cartPriceText,
            cartImageUrl: cartImageUrl
        )
    }

    func dismissAr() {
        presentedAr = nil
    }

    func popMain() {
        guard !mainPath.isEmpty else { return }
        mainPath.removeLast()
    }

    func popMainToRoot() {
        guard !mainPath.isEmpty else { return }
        mainPath.removeLast(mainPath.count)
    }
}
