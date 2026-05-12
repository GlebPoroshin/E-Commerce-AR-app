//
//  VMHolder.swift
//  iosApp
//
//  Created by Глеб Порошин on 10.09.2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import ARApp
import Foundation

/// Специализированная обёртка для KMM `SharedViewModel<S, E, A>`.
/// - Автоматически биндит `viewState: StateFlow` и `viewAction: SharedFlow` к Swift замыканиям.
/// - Позволяет отправлять события в KMM через `sendEvent(_:)`.
/// - Управляет жизненным циклом подписок через `DisposableHandle`.
final class SharedVMHolder<State: AnyObject, Event: AnyObject, Action: AnyObject, VM: SharedViewModel<State, Event, Action>>: ObservableObject {
    let viewModel: VM
    @Published private(set) var state: State
    /// Hold every bind() handle so concurrent subscribers (e.g. screen + coordinator)
    /// can attach independently and all get disposed on stop()/deinit.
    private var disposableHandles: [Kotlinx_coroutines_coreDisposableHandle] = []

    init(viewModel: VM, initialState: State) {
        self.viewModel = viewModel
        self.state = initialState
    }

    func start(onAction: @escaping (Action) -> Void = { _ in }) {
        let handle = FlowWatchUtilsKt.bind(
            state: viewModel.viewState,
            onState: { [weak self] newState in
                if let typedState = newState as? State {
                    DispatchQueue.main.async {
                        self?.state = typedState
                    }
                }
            },
            action: viewModel.viewAction,
            onAction: { action in
                if let typedAction = action as? Action {
                    onAction(typedAction)
                }
            }
        )
        disposableHandles.append(handle)
    }

    func sendEvent(_ event: Event) {
        viewModel.onEvent(event: event)
    }

    func stop() {
        disposableHandles.forEach { $0.dispose() }
        disposableHandles.removeAll()
    }

    deinit {
        stop()
    }
}
