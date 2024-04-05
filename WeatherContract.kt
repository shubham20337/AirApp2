package com.example.airapp3

import android.provider.BaseColumns

object WeatherContract {
    /* Inner class that defines the table contents */
    class WeatherEntry : BaseColumns {
        companion object {
            const val TABLE_NAME = "weather"
            const val COLUMN_DATE = "date"
            const val COLUMN_MAX_TEMP = "max_temp"
            const val COLUMN_MIN_TEMP = "min_temp"
        }
    }
}
