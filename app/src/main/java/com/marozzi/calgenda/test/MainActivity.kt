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

        calgenda.listener = object : CalgendaView.OnCalgendaListener {
            override fun onInitDone() {

            }

            override fun onMonthChange(newMonth: Date) {
                toggle_calendar.text = newMonth.formatDate("MMMM yyyy").toUpperCase(Locale.getDefault())
                AppExecutors.diskIO().execute {
                    mockEvents(newMonth) { events ->
                        AppExecutors.mainThread().execute {
                            calgenda.addEvents(events, true)
                        }
                    }
                }
            }
        }
        calgenda.initCalgenda(CalendarViewHandlerImp(), AgendaViewHandlerImp(), startDate, endDate, Calendar.MONDAY)
    }

    private fun mockEvents(date: Date, callback: (List<Event>) -> Unit) {
        val events = mutableListOf<MockEvent>()
        val current = Calendar.getInstance().apply {
            time = date
        }
        for (i in 0..100) {
            if (Random.nextBoolean()) {
                current.set(Calendar.DAY_OF_MONTH, Random.nextInt(current.getActualMinimum(Calendar.DAY_OF_MONTH), current.getActualMaximum(Calendar.DAY_OF_MONTH)))
                events.add(MockEvent(UUID.randomUUID().toString(), current.time))
            }
        }
        callback.invoke(events)
    }

    class MockEvent(override var id: String, override var date: Date) : Event
}
