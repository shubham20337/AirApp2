package com.example.airapp3

import android.content.ContentValues
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.time.LocalDate

class MainActivity : AppCompatActivity() {

    private lateinit var dateEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var resultTextView: TextView
    private lateinit var dbHelper: WeatherDbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = WeatherDbHelper(this)

        dateEditText = findViewById(R.id.dateEditText)
        searchButton = findViewById(R.id.searchButton)
        resultTextView = findViewById(R.id.resultTextView)

        searchButton.setOnClickListener {
            val date = dateEditText.text.toString()

            if (isTemperatureDataAvailable(date)) {
                val temperatureData = retrieveWeatherDataFromDatabase(date)
                updateUI(temperatureData)
            }else {
                fetchWeatherData(date)
            }
        }
    }

    private fun isTemperatureDataAvailable(date: String): Boolean {
        val db = dbHelper.readableDatabase
        val projection = arrayOf(WeatherContract.WeatherEntry.COLUMN_DATE)
        val selection = "${WeatherContract.WeatherEntry.COLUMN_DATE} = ?"
        val selectionArgs = arrayOf(date)

        val cursor = db.query(
            WeatherContract.WeatherEntry.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        val dataAvailable = cursor.count > 0
        cursor.close()
        return dataAvailable
    }

    private fun retrieveWeatherDataFromDatabase(date: String): String {
        val db = dbHelper.readableDatabase

        val projection = arrayOf(
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP
        )

        val selection = "${WeatherContract.WeatherEntry.COLUMN_DATE} = ?"
        val selectionArgs = arrayOf(date)

        val cursor = db.query(
            WeatherContract.WeatherEntry.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        var temperatureData = ""

        if (cursor.moveToFirst()) {
            val maxTempIndex = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP)
            val minTempIndex = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP)

            val maxTemp = cursor.getInt(maxTempIndex)
            val minTemp = cursor.getInt(minTempIndex)

            temperatureData = "Max Temp: $maxTemp°C, Min Temp: $minTemp°C"
        }

        cursor.close()
        return temperatureData
    }

    private fun fetchWeatherData(date: String) {
        val apiKey = "4a6342ab7e213223499acea64e5818ff" // Replace with your API key from OpenWeatherMap

        val baseUrl = "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/London,UK/"
        val apiUrl = baseUrl + date + "T13:00:00" + "?key=6VCNXTN9NAY69A49GL48ZDAF8"

        GlobalScope.launch(Dispatchers.Main) {
            val result = withContext(Dispatchers.IO) {
                try {
                    val response = URL(apiUrl).readText()
                    val jsonObject = JSONObject(response)
                    val main = jsonObject.getJSONArray("days")
                    val dict = main.getString(0)
                    val dict2 = """$dict""".trimIndent()
                    val json2 = JSONObject(dict2)
                    val tempMax = json2.getInt("tempmax")
                    val tempMin = json2.getInt("tempmin")

                    // Save data to database
                    saveWeatherDataToDatabase(date, tempMax, tempMin)

                    "Max Temp: $tempMax°C, Min Temp: $tempMin°C"
                } catch (e: Exception) {
                    "Error: ${e.message}"
                }
            }
            updateUI(result as String)
        }
    }

    private fun saveWeatherDataToDatabase(date: String, tempMax: Int, tempMin: Int) {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(WeatherContract.WeatherEntry.COLUMN_DATE, date)
            put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, tempMax)
            put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, tempMin)
        }

        db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, values)
    }

    private fun updateUI(text: String) {
        resultTextView.text = text
    }
}
