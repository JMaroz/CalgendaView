package com.marozzi.calgenda.test

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.marozzi.calgenda.view.CalgendaView
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toggle_calendar.setOnClickListener {
            calgenda.toggleCalendar()
        }

        val startDate = Calendar.getInstance().apply {
            add(Calendar.MONTH, -6)
        }.time

        val endDate = Calendar.getInstance().apply {
            add(Calendar.MONTH, 2)
        }.time

        calgenda.initCalgenda(CalendarViewHandlerImp(),
            AgendaViewHandlerImp(),
            startDate,
            endDate,
            Calendar.MONDAY,
            emptyList())

        calgenda.calgendaListener = object : CalgendaView.OnCalgendaListener {
            override fun onMonthChange(newMonth: Date) {
                toggle_calendar.text = newMonth.formatDate("MMMM yyyy").toUpperCase(Locale.getDefault())
            }
        }
    }
}
