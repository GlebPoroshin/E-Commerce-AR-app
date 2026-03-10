package com.poroshin.rut.ar.common.cart.data.db

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.koin.core.context.GlobalContext

actual class DatabaseDriverFactory actual constructor() {
    actual fun createDriver(): SqlDriver = AndroidSqliteDriver(
        schema = CartDatabase.Schema,
        context = GlobalContext.get().get<Context>(),
        name = "cart.db",
    )
}
