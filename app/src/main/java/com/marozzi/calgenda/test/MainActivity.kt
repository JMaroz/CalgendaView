package com.marozzi.calgenda.test

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.marozzi.calgenda.model.Event
import com.marozzi.calgenda.view.CalgendaView
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.random.Random

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

        val endDate = Calendar.getInstance().time

        calgenda.initCalgenda(CalendarViewHandlerImp(), AgendaViewHandlerImp(), startDate, endDate, Calendar.MONDAY, emptyList())
        calgenda.calgendaListener = object : CalgendaView.OnCalgendaListener {
            override fun onMonthChange(newMonth: Date) {
                toggle_calendar.text = newMonth.formatDate("MMMM yyyy").toUpperCase(Locale.getDefault())
                calgenda.postDelayed({
                    calgenda.addEvents(mockEvents(newMonth))
                }, 1000)
            }
        }
    }

    private fun mockEvents(date: Date): List<Event> {
        val events = mutableListOf<MockEvent>()
        val current = Calendar.getInstance().apply {
            time = date
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        }
        for (i in 0..100) {
            current.add(Calendar.DAY_OF_MONTH, Random.nextInt(-1, 1))
            events.add(MockEvent(UUID.randomUUID().toString(), current.time))
        }
        return events
    }

    class MockEvent(override var id: String, override var date: Date) : Event
}
