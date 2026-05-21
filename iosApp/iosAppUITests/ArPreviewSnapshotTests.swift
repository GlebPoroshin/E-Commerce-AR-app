import XCTest
import SwiftUI
import UIKit

private enum PreviewTrackingStatus {
    case tracking
    case searchingSurface
    case lost
}

@available(iOS 16.0, *)
private struct PreviewScaffold: View {
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
                bottomOverlay.padding(.bottom, 24)
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
                errorBanner(errorMessage).padding(.horizontal, 24)
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
            Text(message).font(.body).multilineTextAlignment(.center)
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

final class ArPreviewSnapshotTests: XCTestCase {

    private struct Variant {
        let name: String
        let view: PreviewScaffold
    }

    @MainActor
    func testRenderAllArPreviews() throws {
        guard #available(iOS 16.0, *) else {
            throw XCTSkip("Requires iOS 16+")
        }
        let variants: [Variant] = [
            Variant(name: "01_ar_tracking", view: PreviewScaffold(
                isSingleMode: true, placedModels: 1, isModelLoading: false,
                trackingStatus: .tracking, errorMessage: nil, cartQuantity: 1
            )),
            Variant(name: "02_ar_searching", view: PreviewScaffold(
                isSingleMode: true, placedModels: 0, isModelLoading: true,
                trackingStatus: .searchingSurface, errorMessage: nil, cartQuantity: 0
            )),
            Variant(name: "03_ar_error", view: PreviewScaffold(
                isSingleMode: false, placedModels: 2, isModelLoading: false,
                trackingStatus: .lost, errorMessage: "Не удалось загрузить модель",
                cartQuantity: 2
            )),
        ]

        for v in variants {
            let renderer = ImageRenderer(
                content: v.view.frame(width: 393, height: 852)
            )
            renderer.scale = 3.0
            guard let img = renderer.uiImage, let data = img.pngData() else {
                XCTFail("Failed to render \(v.name)")
                continue
            }
            let att = XCTAttachment(data: data, uniformTypeIdentifier: "public.png")
            att.name = v.name
            att.lifetime = .keepAlways
            add(att)
        }
    }
}
