//
//  VMHolder.swift
//  iosApp
//
//  Created by Глеб Порошин on 10.09.2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import ARApp
import Foundation

// MARK: - Generic VMHolder
final class VMHolder<VM: AnyObject, State: AnyObject, Action: AnyObject>: ObservableObject {
    let vm: VM

    @Published private(set) var state: State
    private var disposableHandle: Kotlinx_coroutines_coreDisposableHandle?

    init(vm: VM, initialState: State) {
        self.vm = vm
        self.state = initialState
    }

    func start(
        stateFlow: Kotlinx_coroutines_coreFlow,
        actionFlow: Kotlinx_coroutines_coreFlow,
        onAction: @escaping (Action) -> Void = { _ in }
    ) {
        disposableHandle = FlowWatchUtilsKt.bind(
            state: stateFlow,
            onState: { [weak self] newState in
                if let typedState = newState as? State {
                    DispatchQueue.main.async {
                        self?.state = typedState
                    }
                }
            },
            action: actionFlow,
            onAction: { action in
                if let typedAction = action as? Action {
                    onAction(typedAction)
                }
            }
        )
    }

    func stop() {
        disposableHandle?.dispose()
        disposableHandle = nil
    }
}

// MARK: - SharedViewModel VMHolder
final class SharedVMHolder<VM: SharedViewModel, State: AnyObject, Action: AnyObject, Event: AnyObject>: ObservableObject {
    let viewModel: VM
    @Published private(set) var state: State
    private var disposableHandle: Kotlinx_coroutines_coreDisposableHandle?
    
    init(viewModel: VM, initialState: State) {
        self.viewModel = viewModel
        self.state = initialState
    }
    
    func start(onAction: @escaping (Action) -> Void = { _ in }) {
        disposableHandle = FlowWatchUtilsKt.bind(
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
    }
    
    func sendEvent(_ event: Event) {
        viewModel.onEvent(event: event)
    }
    
    func stop() {
        disposableHandle?.dispose()
        disposableHandle = nil
    }
    
    deinit {
        stop()
    }
}
