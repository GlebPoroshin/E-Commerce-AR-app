import XCTest

final class ArScreenshotMaker: XCTestCase {

    private let app = XCUIApplication()

    override func setUpWithError() throws {
        continueAfterFailure = false
        app.launchArguments = ["--ar-demo"]
        app.launch()
    }

    private func snap(_ name: String) {
        let att = XCTAttachment(screenshot: XCUIScreen.main.screenshot())
        att.name = name
        att.lifetime = .keepAlways
        add(att)
    }

    func testCaptureArScreen() throws {
        let plp = app.scrollViews["plp_content_scroll"]
        XCTAssertTrue(plp.waitForExistence(timeout: 60))
        sleep(3)

        let predicate = NSPredicate(format: "identifier BEGINSWITH 'product_card_'")
        let cards = app.descendants(matching: .any).matching(predicate)
        XCTAssertGreaterThan(cards.count, 0)
        cards.element(boundBy: 0).tap()

        let downloadBtn = app.buttons["Скачать модель"]
        let viewArBtn = app.buttons["Посмотреть в AR"]

        XCTAssertTrue(
            downloadBtn.waitForExistence(timeout: 30) || viewArBtn.waitForExistence(timeout: 2)
        )

        if downloadBtn.exists {
            downloadBtn.tap()
            // wait for download to finish; AR navigation triggers automatically after.
        } else {
            viewArBtn.tap()
        }

        let modeToggle = app.buttons["ar_mode_toggle"]
        XCTAssertTrue(modeToggle.waitForExistence(timeout: 60))
        sleep(2)
        snap("06_ar")
    }
}
