import SwiftUI
import UIKit

@available(iOS 16.0, *)
private struct ArScreenPreviewScaffold: View {
    let isSingleMode: Bool
    let placedModels: Int
    let isModelLoading: Bool
    let trackingStatus: PreviewTrackingStatus
    let errorMessage: String?
    let cartQuantity: Int

    var body: some View {
        ZStack(alignment: .top) {
            Color(.lightGray).ignoresSafeArea()

            VStack(spacing: 12) {
                topControls
                Spacer()
                bottomOverlay
                    .padding(.bottom, 24)
            }
            .padding(.horizontal, 16)
            .padding(.top, 12)
        }
    }

    private var topControls: some View {
        VStack(spacing: 12) {
            HStack(spacing: 12) {
                chip(text: isSingleMode ? "Режим: одна" : "Режим: несколько", enabled: true)
                chip(text: "Очистить", enabled: placedModels > 0)
            }

            if isModelLoading {
                ProgressView()
                    .progressViewStyle(.linear)
                    .frame(width: 160)
                    .padding(.top, 4)
            }

            if let msg = trackingMessage {
                pill(msg)
            }

            if placedModels > 0 {
                pill("Объектов в сцене: \(placedModels)")
            }
        }
    }

    private var bottomOverlay: some View {
        VStack(spacing: 12) {
            if let errorMessage {
                errorBanner(errorMessage)
                    .padding(.horizontal, 24)
            }
            cartPicker
        }
    }

    private var cartPicker: some View {
        HStack(spacing: 8) {
            if cartQuantity > 0 {
                Button("-") {}.buttonStyle(.bordered)
                pill("\(cartQuantity)")
                Button("+") {}.buttonStyle(.borderedProminent)
            } else {
                Button("Добавить в корзину") {}.buttonStyle(.borderedProminent)
            }
        }
    }

    private var trackingMessage: String? {
        switch trackingStatus {
        case .searchingSurface: return "Ищем подходящую плоскость — перемещайте устройство."
        case .lost: return "Трекинг потерян. Наведите камеру на освещенную поверхность."
        case .tracking: return nil
        }
    }

    private func chip(text: String, enabled: Bool) -> some View {
        Text(text)
            .font(.subheadline)
            .padding(.horizontal, 12)
            .padding(.vertical, 6)
            .background(
                Capsule().stroke(
                    enabled ? Color.primary.opacity(0.4) : Color.primary.opacity(0.15),
                    lineWidth: 1
                )
            )
            .foregroundColor(enabled ? .primary : .secondary)
    }

    private func pill(_ text: String) -> some View {
        Text(text)
            .font(.caption)
            .foregroundColor(.white)
            .padding(.horizontal, 8)
            .padding(.vertical, 4)
            .background(Color.black.opacity(0.55))
            .clipShape(RoundedRectangle(cornerRadius: 4))
    }

    private func errorBanner(_ message: String) -> some View {
        VStack(spacing: 12) {
            Text(message)
                .font(.body)
                .multilineTextAlignment(.center)
            Button("Повторить") {}.buttonStyle(.borderedProminent)
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 16)
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(Color(UIColor.systemBackground))
                .shadow(radius: 4)
        )
    }
}

private enum PreviewTrackingStatus {
    case tracking
    case searchingSurface
    case lost
}

@available(iOS 16.0, *)
struct ArScreenPreview_Previews: PreviewProvider {
    static var previews: some View {
        Group {
            ArScreenPreviewScaffold(
                isSingleMode: true,
                placedModels: 1,
                isModelLoading: false,
                trackingStatus: .tracking,
                errorMessage: nil,
                cartQuantity: 1
            )
            .previewDisplayName("AR — Tracking")

            ArScreenPreviewScaffold(
                isSingleMode: true,
                placedModels: 0,
                isModelLoading: true,
                trackingStatus: .searchingSurface,
                errorMessage: nil,
                cartQuantity: 0
            )
            .previewDisplayName("AR — Searching")

            ArScreenPreviewScaffold(
                isSingleMode: false,
                placedModels: 2,
                isModelLoading: false,
                trackingStatus: .lost,
                errorMessage: "Не удалось загрузить модель",
                cartQuantity: 2
            )
            .previewDisplayName("AR — Error")
        }
        .previewDevice("iPhone 15 Pro")
    }
}
