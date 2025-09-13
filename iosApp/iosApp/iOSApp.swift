import SwiftUI
import ARApp

@main
struct iOSApp: App {
    init() {
        KoinInitKt.doInitKoin()
    }
	var body: some Scene {
		WindowGroup {
			if #available(iOS 16.0, *) {
				PlpScreen()
			} else {
				Text("iOS 16+ required")
			}
		}
	}
}
