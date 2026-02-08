import SwiftUI
import ARApp
import Foundation

@available(iOS 16.0, *)
struct CartScreen: View {
    @StateObject private var holder = SharedVMHolder<CartState, CartEvent, CartAction, CartViewModel>(
        viewModel: CartViewModel(),
        initialState: CartState.Loading()
    )

    @EnvironmentObject private var router: AppRouter

    var body: some View {
        content
            .onAppear {
                holder.start { action in
                    switch action {
                    case let a as CartAction.OpenPdp:
                        router.openProductFromCart(sku: a.sku)
                    case _ as CartAction.SwitchToMainTab:
                        router.showMainTab()
                    default:
                        break
                    }
                }
                holder.sendEvent(CartEvent.OnCreate())
            }
            .onDisappear { holder.stop() }
    }

    @ViewBuilder
    private var content: some View {
        switch holder.state {
        case _ as CartState.Loading:
            ProgressView().padding()

        case let content as CartState.Content:
            let lines = castLines(content.lines)
            if lines.isEmpty {
                VStack(spacing: 12) {
                    Text("Корзина пуста")
                        .font(.title2)
                    Button("Перейти на главную") {
                        holder.sendEvent(CartEvent.OnMainClick())
                    }
                    .buttonStyle(.borderedProminent)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else {
                VStack(spacing: 0) {
                    List {
                        ForEach(Array(lines.enumerated()), id: \.offset) { _, line in
                            CartRow(
                                line: line,
                                onIncrease: {
                                    holder.sendEvent(CartEvent.OnIncrease(snapshot: line.snapshot))
                                },
                                onDecrease: {
                                    holder.sendEvent(CartEvent.OnDecrease(sku: line.snapshot.sku))
                                },
                                onRemove: {
                                    holder.sendEvent(CartEvent.OnRemove(sku: line.snapshot.sku))
                                },
                                onOpen: {
                                    holder.sendEvent(CartEvent.OnItemClick(sku: line.snapshot.sku))
                                }
                            )
                        }
                    }
                    .listStyle(.plain)

                    VStack(alignment: .leading, spacing: 6) {
                        Text("Итого: \(content.summary.totalAmountRubles) ₽")
                            .font(.headline)
                        if content.summary.hasInvalidPriceItems {
                            Text("Некорректные цены: \(content.summary.invalidItemsCount). Эти позиции считаются как 0.")
                                .font(.footnote)
                                .foregroundStyle(.secondary)
                        }
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding()
                }
            }

        default:
            EmptyView()
        }
    }

    private func castLines(_ value: Any) -> [CartLine] {
        if let typed = value as? [CartLine] {
            return typed
        }

        if let nsArray = value as? NSArray {
            return nsArray.compactMap { $0 as? CartLine }
        }

        return []
    }
}

@available(iOS 16.0, *)
private struct CartRow: View {
    let line: CartLine
    let onIncrease: () -> Void
    let onDecrease: () -> Void
    let onRemove: () -> Void
    let onOpen: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            AsyncImage(url: URL(string: line.snapshot.imageUrl)) { phase in
                switch phase {
                case .success(let image):
                    image
                        .resizable()
                        .scaledToFill()
                default:
                    Color.gray.opacity(0.25)
                }
            }
            .frame(width: 56, height: 56)
            .clipShape(RoundedRectangle(cornerRadius: 8))

            VStack(alignment: .leading, spacing: 4) {
                Text(line.snapshot.name)
                    .font(.subheadline)
                    .lineLimit(2)
                Text(line.snapshot.priceText)
                    .font(.footnote)
                    .foregroundStyle(.secondary)

                HStack(spacing: 8) {
                    Button("<", action: onDecrease)
                        .buttonStyle(.bordered)
                    Text("\(Int(line.quantity))")
                        .font(.headline)
                    Button(">", action: onIncrease)
                        .buttonStyle(.borderedProminent)
                }
            }

            Spacer()

            Button("Удалить", action: onRemove)
                .buttonStyle(.bordered)
        }
        .contentShape(Rectangle())
        .onTapGesture(perform: onOpen)
    }
}
