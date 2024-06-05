package com.tw.timewise

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AddEventActivity : AppCompatActivity() {

    private lateinit var eventName: EditText
    private lateinit var startDate: TextView
    private lateinit var startTime: TextView
    private lateinit var endDate: TextView
    private lateinit var endTime: TextView
    private lateinit var saveEventButton: Button
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_event)

        eventName = findViewById(R.id.eventName)
        startDate = findViewById(R.id.startDate)
        startTime = findViewById(R.id.startTime)
        endDate = findViewById(R.id.endDate)
        endTime = findViewById(R.id.endTime)
        saveEventButton = findViewById(R.id.saveEventButton)

        startDate.setOnClickListener { showDatePickerDialog(startDate) }
        startTime.setOnClickListener { showTimePickerDialog(startTime) }
        endDate.setOnClickListener { showDatePickerDialog(endDate) }
        endTime.setOnClickListener { showTimePickerDialog(endTime) }

        saveEventButton.setOnClickListener {
            onSaveButtonClicked()
        }
    }

    private fun showDatePickerDialog(dateView: TextView) {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            calendar.set(selectedYear, selectedMonth, selectedDay)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateView.text = sdf.format(calendar.time)
        }, year, month, day).show()
    }

    private fun showTimePickerDialog(timeView: TextView) {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
            calendar.set(Calendar.MINUTE, selectedMinute)
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            timeView.text = sdf.format(calendar.time)
        }, hour, minute, true).show()
    }

    private fun onSaveButtonClicked() {
        val name = eventName.text.toString()
        val start = "${startDate.text}T${startTime.text}:00Z"
        val end = "${endDate.text}T${endTime.text}:00Z"

        if (name.isEmpty() || startDate.text.isEmpty() || startTime.text.isEmpty() || endDate.text.isEmpty() || endTime.text.isEmpty()) {
            Toast.makeText(this, "Musisz wypełnić wszystkie pola", Toast.LENGTH_SHORT).show()
            return
        }

        saveEvent(name, start, end)
    }

    private fun saveEvent(name: String, startTime: String, endTime: String) {
        val token = getToken()
        if (token == null) {
            Toast.makeText(this, "Aby dodać, musisz się zalogować", Toast.LENGTH_SHORT).show()
            return
        }

        val client = OkHttpClient()
        val jsonObject = JSONObject()
        jsonObject.put("name", name)
        jsonObject.put("start_time", startTime)
        jsonObject.put("end_time", endTime)

        val requestBody = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), jsonObject.toString())

        val request = Request.Builder()
            .url("https://calendar.carians.smallhost.pl/api/events/")
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("AddEventActivity", "Błąd API: ", e)
                runOnUiThread {
                    Toast.makeText(this@AddEventActivity, "Błąd przy zapisie wydarzenia", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                Log.d("AddEventActivity", "Response received: $responseData")
                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(this@AddEventActivity, "Wydarzenie zapisane", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@AddEventActivity, "Błąd przy zapisie wydarzenia", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun getToken(): String? {
        val sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("auth_token", null)
    }

    fun onBackButtonClicked(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
