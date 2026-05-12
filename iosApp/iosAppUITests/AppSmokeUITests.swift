import XCTest

final class AppSmokeUITests: XCTestCase {

    private let app = XCUIApplication()

    override func setUpWithError() throws {
        continueAfterFailure = false
        app.launch()
    }

    override func tearDownWithError() throws {
        app.terminate()
    }

    // MARK: - Test 1: App launches and shows PLP

    /// Приложение запускается и отображает экран каталога (PLP).
    /// PLP-контент идентифицируется по accessibilityIdentifier "plp_content_scroll".
    func testAppLaunchesAndShowsPlp() throws {
        let plpScroll = app.scrollViews["plp_content_scroll"]
        XCTAssertTrue(
            plpScroll.waitForExistence(timeout: 15),
            "Ожидался скролл-контейнер PLP (plp_content_scroll), но он не появился"
        )
        XCTAssertTrue(plpScroll.isHittable)

        // Screenshot для документации
        let attachment = XCTAttachment(screenshot: app.screenshot())
        attachment.name = "PLP_loaded"
        attachment.lifetime = .keepAlways
        add(attachment)
    }

    // MARK: - Test 2: PLP → PDP navigation

    /// Тап по первой карточке товара открывает PDP.
    /// PDP идентифицируется по кнопке «Скачать модель» или «Посмотреть в AR».
    func testPlpToPdpNavigation() throws {
        // Ждём загрузки PLP-контента
        let plpScroll = app.scrollViews["plp_content_scroll"]
        XCTAssertTrue(
            plpScroll.waitForExistence(timeout: 15),
            "PLP контент не загрузился за 15 секунд"
        )

        // Ищем первую доступную карточку по паттерну accessibilityIdentifier
        let cards = app.otherElements.matching(NSPredicate(format: "identifier BEGINSWITH 'product_card_'"))
        if cards.count > 0 {
            cards.element(boundBy: 0).tap()
        } else {
            // Fallback: тапаем в центр первого видимого элемента PLP
            plpScroll.children(matching: .any).element(boundBy: 0).tap()
        }

        // Ждём появления PDP (кнопка «Скачать модель» или «Посмотреть в AR»)
        let downloadButton = app.buttons["Скачать модель"]
        let viewArButton = app.buttons["Посмотреть в AR"]

        let pdpAppeared = downloadButton.waitForExistence(timeout: 10)
            || viewArButton.waitForExistence(timeout: 2)

        XCTAssertTrue(pdpAppeared, "PDP не открылся: кнопка 'Скачать модель' или 'Посмотреть в AR' не найдена")
    }

    // MARK: - Test 3: PDP → AR navigation

    /// Тап по «Скачать модель» / «Посмотреть в AR» переводит на AR-экран.
    /// На симуляторе ARKit не работает — тест проверяет либо загрузочный экран AR,
    /// либо экран ошибки «Закрыть», либо кнопку «ar_mode_toggle».
    func testPdpToArNavigation() throws {
        // Перейти на PDP
        let plpScroll = app.scrollViews["plp_content_scroll"]
        XCTAssertTrue(plpScroll.waitForExistence(timeout: 15))

        let cards = app.otherElements.matching(NSPredicate(format: "identifier BEGINSWITH 'product_card_'"))
        if cards.count > 0 {
            cards.element(boundBy: 0).tap()
        } else {
            plpScroll.children(matching: .any).element(boundBy: 0).tap()
        }

        let downloadButton = app.buttons["Скачать модель"]
        let viewArButton = app.buttons["Посмотреть в AR"]
        XCTAssertTrue(
            downloadButton.waitForExistence(timeout: 10) || viewArButton.waitForExistence(timeout: 2)
        )

        // Тапаем по кнопке перехода в AR
        if downloadButton.exists {
            downloadButton.tap()
        } else {
            viewArButton.tap()
        }

        // AR-экран: либо loading (ProgressView с текстом), либо error (кнопка «Закрыть»),
        // либо полноценные контролы (ar_mode_toggle). Любой из них подтверждает открытие AR.
        let modeToggle = app.buttons["ar_mode_toggle"]
        let closeButton = app.buttons["ar_error_close_btn"]
        let loadingText = app.staticTexts["Подождите, идёт подготовка AR"]

        let arScreenAppeared = modeToggle.waitForExistence(timeout: 10)
            || closeButton.waitForExistence(timeout: 2)
            || loadingText.waitForExistence(timeout: 2)

        XCTAssertTrue(arScreenAppeared, "AR-экран не открылся: ни loading, ни error, ни контролы не найдены")
    }

    // MARK: - Test 4 (MVI): AR mode toggle changes state

    /// Тап по кнопке режима (ar_mode_toggle) отправляет ArEvent.SetSingleMode,
    /// ViewModel вызывает updateState(isSingleMode), viewState обновляется,
    /// SwiftUI перерисовывает кнопку: "1" → "N" → "1".
    ///
    /// Это прямое доказательство MVI-цикла на iOS:
    /// UI event → shared ArViewModel → StateFlow → SwiftUI View.
    func testArMviModeSwitchCycle() throws {
        // Перейти до AR-экрана
        let plpScroll = app.scrollViews["plp_content_scroll"]
        XCTAssertTrue(plpScroll.waitForExistence(timeout: 15))

        let cards = app.otherElements.matching(NSPredicate(format: "identifier BEGINSWITH 'product_card_'"))
        if cards.count > 0 {
            cards.element(boundBy: 0).tap()
        } else {
            plpScroll.children(matching: .any).element(boundBy: 0).tap()
        }

        let downloadButton = app.buttons["Скачать модель"]
        let viewArButton = app.buttons["Посмотреть в AR"]
        XCTAssertTrue(
            downloadButton.waitForExistence(timeout: 10) || viewArButton.waitForExistence(timeout: 2)
        )
        if downloadButton.exists { downloadButton.tap() } else { viewArButton.tap() }

        // Ждём кнопку режима (она рендерится только если AR-сцена загрузилась)
        let modeToggle = app.buttons["ar_mode_toggle"]
        guard modeToggle.waitForExistence(timeout: 12) else {
            // На симуляторе AR не работает — кнопка режима недоступна.
            // Проверяем наличие хотя бы экрана ошибки/загрузки, чтобы подтвердить,
            // что навигация состоялась, и пропускаем MVI-проверку.
            let closeButton = app.buttons["ar_error_close_btn"]
            let loadingText = app.staticTexts["Подождите, идёт подготовка AR"]
            let arReached = closeButton.waitForExistence(timeout: 5)
                || loadingText.waitForExistence(timeout: 2)
            XCTAssertTrue(arReached, "AR-экран не открылся даже в error/loading состоянии")
            throw XCTSkip("ar_mode_toggle недоступен (ARKit не поддерживается на симуляторе) — MVI-проверка пропущена")
        }

        // Проверяем начальное состояние: isSingleMode=true → кнопка показывает "1"
        XCTAssertEqual(modeToggle.label, "1", "Ожидался текст '1' (isSingleMode=true)")

        // Тап → ArEvent.SetSingleMode(false) → updateState → viewState.isSingleMode=false → "N"
        modeToggle.tap()
        let labelChangedToN = modeToggle.waitForExistence(timeout: 3)
        XCTAssertTrue(labelChangedToN)
        XCTAssertEqual(modeToggle.label, "N", "После первого тапа ожидался текст 'N' (isSingleMode=false)")

        // Второй тап → SetSingleMode(true) → обратно "1"
        modeToggle.tap()
        XCTAssertEqual(modeToggle.label, "1", "После второго тапа ожидался возврат к '1' (isSingleMode=true)")
    }
}
