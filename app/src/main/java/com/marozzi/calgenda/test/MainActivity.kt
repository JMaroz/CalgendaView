package com.marozzi.calgenda.test

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.marozzi.calgenda.model.Event
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
            add(Calendar.MONTH, -11)
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        }.time

        val endDate = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        }.time

        calgenda.initCalgenda(CalendarViewHandlerImp(),
            AgendaViewHandlerImp(),
            startDate,
            endDate,
            Calendar.MONDAY,
            mockEvents())

        calgenda.calgendaListener = object : CalgendaView.OnCalgendaListener {
            override fun onMonthChange(newMonth: Date) {
                toggle_calendar.text = newMonth.formatDate("MMMM yyyy").toUpperCase(Locale.getDefault())
            }
        }
    }

    private fun mockEvents() : List<Event> {

        return emptyList()
    }
}
