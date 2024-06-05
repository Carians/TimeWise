package com.tw.timewise

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class DayViewActivity : AppCompatActivity() {

    private lateinit var eventTable: TableLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_day_view)

        eventTable = findViewById(R.id.eventTable)

        fetchEvents()
    }

    private fun fetchEvents() {
        val token = getToken()
        if (token == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://calendar.carians.smallhost.pl/api/events/")
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("DayViewActivity", "Request failed", e)
                runOnUiThread {
                    Toast.makeText(this@DayViewActivity, "Failed to fetch events", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                Log.d("DayViewActivity", "Response received: $responseData")
                if (response.isSuccessful && responseData != null) {
                    val json = JSONObject(responseData)
                    val results = json.getJSONArray("results")
                    val todayEvents = filterTodayEvents(results)
                    runOnUiThread {
                        displayEvents(todayEvents)
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@DayViewActivity, "Failed to fetch events", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun filterTodayEvents(events: JSONArray): JSONArray {
        val todayEvents = JSONArray()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayDate = sdf.format(Date())

        for (i in 0 until events.length()) {
            val event = events.getJSONObject(i)
            val startTime = event.getString("start_time")
            if (startTime.startsWith(todayDate)) {
                todayEvents.put(event)
            }
        }

        return todayEvents
    }

    private fun displayEvents(events: JSONArray) {
        eventTable.removeAllViews()

        // Add table headers
        val headerRow = TableRow(this)
        headerRow.addView(createTextView("Nazwa"))
        headerRow.addView(createTextView("Rozpoczęcie"))
        headerRow.addView(createTextView("Zakończenie"))
        eventTable.addView(headerRow)

        // Add events to table
        for (i in 0 until events.length()) {
            val event = events.getJSONObject(i)
            val row = TableRow(this)
            row.addView(createTextView(event.getString("name")))
            row.addView(createTextView(formatTime(event.getString("start_time"))))
            row.addView(createTextView(formatTime(event.getString("end_time"))))
            eventTable.addView(row)
        }
    }

    private fun createTextView(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 18f
            setPadding(8, 8, 8, 8)
        }
    }

    private fun formatTime(time: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = inputFormat.parse(time)
        return outputFormat.format(date!!)
    }

    private fun getToken(): String? {
        val sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("auth_token", null)
    }

    fun onBackButtonClicked(view: View) {
        finish()
    }
}
