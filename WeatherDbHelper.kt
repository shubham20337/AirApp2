package com.example.airapp3

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class WeatherDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        val SQL_CREATE_WEATHER_TABLE = """
            CREATE TABLE ${WeatherContract.WeatherEntry.TABLE_NAME} (
                ${WeatherContract.WeatherEntry.COLUMN_DATE} TEXT PRIMARY KEY,
                ${WeatherContract.WeatherEntry.COLUMN_MAX_TEMP} INTEGER,
                ${WeatherContract.WeatherEntry.COLUMN_MIN_TEMP} INTEGER
            )
        """.trimIndent()
        db.execSQL(SQL_CREATE_WEATHER_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL("DROP TABLE IF EXISTS ${WeatherContract.WeatherEntry.TABLE_NAME}")
        onCreate(db)
    }

    companion object {
        // If you change the database schema, you must increment the database version.
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "Weather.db"
    }
}
