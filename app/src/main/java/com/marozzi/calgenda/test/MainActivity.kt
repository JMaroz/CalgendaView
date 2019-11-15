package com.marozzi.calgenda.test

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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

        calgenda.initCalgenda(CalendarViewHandlerImp(), AgendaViewHandlerImp(), startDate, endDate, Calendar.MONDAY, mockEvents())

        /*

        app:cg_week_header_unselected_alpha=".6"
        app:cg_week_header_text_size="5sp"
        app:cg_week_header_background_color="@color/colorPrimary"
        app:cg_week_header_weekdays_color="@color/white"
        app:cg_week_header_weekend_color="@color/white"
         */
        calgenda.setHeaderCustomizations(ContextCompat.getColor(this, R.color.colorPrimary), Color.WHITE, Color.WHITE, resources.getDimensionPixelSize(R.dimen.dimen_5dp), .6f)
        calgenda.calgendaListener = object : CalgendaView.OnCalgendaListener {
            override fun onMonthChange(newMonth: Date) {
                toggle_calendar.text = newMonth.formatDate("MMMM yyyy").toUpperCase(Locale.getDefault())
            }
        }
    }

    private fun mockEvents(): List<Event> {

        return emptyList()
    }
}
