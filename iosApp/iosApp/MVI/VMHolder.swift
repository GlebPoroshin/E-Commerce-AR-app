//
//  VMHolder.swift
//  iosApp
//
//  Created by Глеб Порошин on 10.09.2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import Foundation

final class VMHolder<VM: AnyObject, State: AnyObject, Action: AnyObject>: ObservableObject {
    let vm: VM
    @Published private(set) var state: State
    private var bag: ?

    init(vm: VM, initialState: State) {
        self.vm = vm
        self.state = initialState
    }

    func start(
        bind: (VM, @escaping (State) -> Void, @escaping (Action) -> Void) -> Kotlinx_coroutines_coreDisposableHandle,
        onAction: @escaping (Action) -> Void
    ) {
        bag = bind(vm, { [weak self] s in self?.state = s }, onAction)
    }

    func stop() { bag?.dispose(); bag = nil }
}
