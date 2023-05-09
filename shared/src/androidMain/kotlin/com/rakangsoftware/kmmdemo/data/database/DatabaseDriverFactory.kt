package com.rakangsoftware.kmmdemo.data.database

import android.content.Context
import com.rakangsoftware.kmmdemo.database.AppDatabase
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(AppDatabase.Schema, context, "database.db")
    }
}
