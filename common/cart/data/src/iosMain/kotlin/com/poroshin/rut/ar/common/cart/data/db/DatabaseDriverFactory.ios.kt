package com.poroshin.rut.ar.common.cart.data.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

actual class DatabaseDriverFactory actual constructor() {
    actual fun createDriver(): SqlDriver = NativeSqliteDriver(
        schema = CartDatabase.Schema,
        name = "cart.db",
    )
}
