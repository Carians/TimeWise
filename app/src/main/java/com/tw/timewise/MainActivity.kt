package com.tw.timewise

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CalendarView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var addEventButton: Button
    private lateinit var dayViewButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        calendarView = findViewById(R.id.calendarView)
        addEventButton = findViewById(R.id.addEventButton)
        dayViewButton = findViewById(R.id.dayViewButton)

        addEventButton.setOnClickListener {
            val intent = Intent(this, AddEventActivity::class.java)
            startActivity(intent)
        }

        dayViewButton.setOnClickListener {
            val intent = Intent(this, DayViewActivity::class.java)
            startActivity(intent)
        }
    }
}