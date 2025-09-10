import SwiftUI
import ARApp

struct ContentView: View {
	@StateObject private var holder = SharedVMHolder<PlpState, PlpEvent, PlpAction, PlpTestViewModel>(
		viewModel: PlpTestViewModel(),
		initialState: PlpState(counter: 0)
	)

	var body: some View {
		VStack(spacing: 12) {
			Text("Counter: \(holder.state.counter)")
            Button("+") { holder.sendEvent(PlpEvent.Increment()) }
            Button("-") { holder.sendEvent(PlpEvent.Decrement()) }
		}
		.onAppear { holder.start { action in /* handle actions if needed */ } }
		.onDisappear { holder.stop() }
	}
}

struct ContentView_Previews: PreviewProvider {
	static var previews: some View {
		ContentView()
	}
}
