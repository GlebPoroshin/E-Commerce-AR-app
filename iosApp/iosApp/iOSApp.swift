import SwiftUI
import ARApp

@main
struct iOSApp: App {
    init() {
        KoinInitKt.doInitKoin()
    }
	var body: some Scene {
		WindowGroup {
			PlpScreen()
		}
	}
}
