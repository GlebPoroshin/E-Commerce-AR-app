import SwiftUI

enum AppRoute: Hashable {
    case productDetail(sku: Int64)
    case arObject(filePath: String, widthMm: Int, heightMm: Int, depthMm: Int)
}

final class AppRouter: ObservableObject {
    @Published var path = NavigationPath()

    func push(_ route: AppRoute) {
        path.append(route)
    }

    func pop() {
        guard !path.isEmpty else { return }
        path.removeLast()
    }

    func popToRoot() {
        guard !path.isEmpty else { return }
        path.removeLast(path.count)
    }
}


