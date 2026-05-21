import XCTest

final class ScreenshotMaker: XCTestCase {

    private let app = XCUIApplication()

    override func setUpWithError() throws {
        continueAfterFailure = false
        app.launch()
    }

    private func snap(_ name: String) {
        let att = XCTAttachment(screenshot: XCUIScreen.main.screenshot())
        att.name = name
        att.lifetime = .keepAlways
        add(att)
    }

    func testCaptureAllScreens() throws {
        let plp = app.scrollViews["plp_content_scroll"]
        XCTAssertTrue(plp.waitForExistence(timeout: 60))
        sleep(6)
        snap("01_plp")

        func openCard(at index: Int) {
            let predicate = NSPredicate(format: "identifier BEGINSWITH 'product_card_'")
            let any = app.descendants(matching: .any).matching(predicate)
            if any.count > index {
                any.element(boundBy: index).tap()
                return
            }
            plp.children(matching: .any).element(boundBy: index).tap()
        }

        openCard(at: 0)

        let addToCart = app.buttons["Добавить в корзину"]
        XCTAssertTrue(addToCart.waitForExistence(timeout: 30))
        sleep(4)
        snap("02_pdp")

        addToCart.tap()
        let plusBtn = app.buttons["+"]
        XCTAssertTrue(plusBtn.waitForExistence(timeout: 5))
        snap("03_pdp_in_cart")

        plusBtn.tap()

        app.navigationBars.buttons.element(boundBy: 0).tap()
        sleep(2)

        openCard(at: 2)
        let addToCart2 = app.buttons["Добавить в корзину"]
        XCTAssertTrue(addToCart2.waitForExistence(timeout: 30))
        addToCart2.tap()
        sleep(2)

        app.tabBars.buttons["Корзина"].tap()
        let removeBtn = app.buttons["Удалить"]
        XCTAssertTrue(removeBtn.waitForExistence(timeout: 10))
        sleep(3)
        snap("04_cart_with_items")

        while app.buttons["Удалить"].exists {
            app.buttons["Удалить"].firstMatch.tap()
            sleep(1)
        }
        XCTAssertTrue(app.staticTexts["Корзина пуста"].waitForExistence(timeout: 5))
        snap("05_cart_empty")
    }

    func testPdpModelDownloadError() throws {
        let plp = app.scrollViews["plp_content_scroll"]
        XCTAssertTrue(plp.waitForExistence(timeout: 60))
        sleep(4)

        let predicate = NSPredicate(format: "identifier BEGINSWITH 'product_card_'")
        let cards = app.descendants(matching: .any).matching(predicate)
        if cards.count > 0 {
            cards.element(boundBy: 0).tap()
        } else {
            plp.children(matching: .any).element(boundBy: 0).tap()
        }

        let downloadBtn = app.buttons["Скачать модель"]
        XCTAssertTrue(downloadBtn.waitForExistence(timeout: 30))
        sleep(10)

        downloadBtn.tap()

        let alert = app.alerts.firstMatch
        XCTAssertTrue(alert.waitForExistence(timeout: 30))
        sleep(1)
        snap("pdp_model_error")
    }
}
