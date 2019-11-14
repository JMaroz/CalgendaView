package com.marozzi.calgenda.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.marozzi.calgenda.R
import com.marozzi.calgenda.adapter.AgendaViewHandler
import com.marozzi.calgenda.adapter.CalendarViewHandler
import com.marozzi.calgenda.model.*
import com.marozzi.calgenda.util.*
import kotlinx.android.synthetic.main.calgenda_view.view.*
import java.util.*

/**
 * Created by amarozzi on 2019-11-04
 */
class CalgendaView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(
    context,
    attrs,
    defStyleAttr) {

    /**
     * Map where for every day has a list of agenda items
     */
    private var calgendaDataMap = TreeMap<String, MutableList<AgendaEventItem<Any>>>()

    var calgendaListener: OnCalgendaListener? = null

    init {
        addView(LayoutInflater.from(context).inflate(R.layout.calgenda_view, this, false))

        val a = context.obtainStyledAttributes(attrs, R.styleable.CalgendaView)

        setBackgroundColor(a.getColor(R.styleable.CalgendaView_cg_background_color, Color.WHITE))

        val headerBackgroundColor = a.getColor(R.styleable.CalgendaView_cg_week_header_background_color,
            Color.WHITE)
        val headerWeekdaysColor = a.getColor(R.styleable.CalgendaView_cg_week_header_weekdays_color,
            Color.LTGRAY)
        val headerWeekendColor = a.getColor(R.styleable.CalgendaView_cg_week_header_weekend_color,
            headerWeekdaysColor)
        val headerTextSize = a.getDimensionPixelSize(R.styleable.CalgendaView_cg_week_header_text_size,
            resources.getDimensionPixelSize(R.dimen.cg_week_header_text_size))
        val headerUnSelectedAlpha = a.getFloat(R.styleable.CalgendaView_cg_week_header_unselected_alpha, 1f)

        a.recycle()

        calendar_week_headbar_view.setCustomizations(headerBackgroundColor,
            headerWeekdaysColor,
            headerWeekendColor,
            headerTextSize,
            headerUnSelectedAlpha)

        agenda_view.listener = object : AgendaView.OnAgendaViewListener {

            override fun onMonthChange(date: Date) {
                calgendaListener?.onMonthChange(date)
            }

            override fun onScrollChange() {
                calendar_view.changeStatus(CalendarView.CalendarViewStatus.COLLAPSE)
            }

            override fun onDateChange(date: Date) {
                calendar_week_headbar_view.setCurrentSelectedDay(date.get(Calendar.DAY_OF_WEEK))
                calendar_view.postDelayed({
                    calendar_view.scrollToDate(date)
                }, 200)
            }
        }

        calendar_view.listener = object : CalendarView.OnCalendarViewListener {

            override fun onMonthChange(date: Date) {
                calendar_week_headbar_view.setCurrentSelectedDay(date.get(Calendar.DAY_OF_WEEK))
                calgendaListener?.onMonthChange(date)
            }

            override fun onCalendarItemSelected(calendarItem: CalendarItem) {
                agenda_view.moveToDate(calendarItem.date)
            }
        }
    }

    fun initCalgenda(calendarViewHandler: CalendarViewHandler, agendaViewHandler: AgendaViewHandler, startDate: Date, endDate: Date, startWeekDay: Int, events: List<Event>) {

        calendar_week_headbar_view.initWeek(startWeekDay)

        calendar_view.setCalendarViewHandler(calendarViewHandler)
        agenda_view.setAgendaViewHandler(agendaViewHandler)

        //init agenda date list, data 7(col) * 65(row) = 455 (cell)
        calgendaDataMap.clear()

        val todayCalendar1 = Calendar.getInstance().apply {
            firstDayOfWeek = startWeekDay
        }
        val todayCalendar2 = Calendar.getInstance().apply {
            firstDayOfWeek = startWeekDay
        }

        //init current week days
        val todayOfWeek = todayCalendar1.get(Calendar.DAY_OF_WEEK)
        for (i in todayOfWeek downTo startWeekDay) {
            calgendaDataMap[todayCalendar1.time.formatDate(CALGENDA_DATE_FORMAT)] = mutableListOf()
            todayCalendar1.add(Calendar.DAY_OF_MONTH, -1)
        }
        todayCalendar2.add(Calendar.DAY_OF_MONTH, 1) //tomorrow
        for (i in todayOfWeek + 1..Calendar.SATURDAY + 1) {
            calgendaDataMap[todayCalendar2.time.formatDate(CALGENDA_DATE_FORMAT)] = mutableListOf()
            todayCalendar2.add(Calendar.DAY_OF_MONTH, 1)
        }

        //init row before the current week days
        var min = startDate.daysBetween(todayCalendar1.time)
        while (min % 7 != 0) {
            min++
        }
        val beforeSize = min
        for (i in 0 until beforeSize) {
            calgendaDataMap[todayCalendar1.time.formatDate(CALGENDA_DATE_FORMAT)] = mutableListOf()
            todayCalendar1.add(Calendar.DAY_OF_MONTH, -1)
        }

        //init row after the current week days
        var max = todayCalendar2.time.daysBetween(endDate)
        while (max % 7 != 0) {
            max++
        }
        val afterSize = max
        for (i in 0 until afterSize) {
            calgendaDataMap[todayCalendar2.time.formatDate(CALGENDA_DATE_FORMAT)] = mutableListOf()
            todayCalendar2.add(Calendar.DAY_OF_MONTH, 1)
        }

        setEvents(events)

        val today = Calendar.getInstance().time
        agenda_view.moveToDate(today)
        calendar_view.scrollToDate(today)
    }

    fun toggleCalendar() {
        calendar_view.changeStatus(if (calendar_view.status == CalendarView.CalendarViewStatus.COLLAPSE) {
            CalendarView.CalendarViewStatus.EXPAND
        } else {
            CalendarView.CalendarViewStatus.COLLAPSE
        })
    }

    fun setEvents(events: List<Event>) {
        calgendaDataMap.values.forEach { it.clear() }
        addEvents(events)
    }

    fun addEvents(events: List<Event>) {
        events.forEach {
            val date = it.date.formatDate(CALGENDA_DATE_FORMAT)
            calgendaDataMap[date] = (calgendaDataMap[date] ?: mutableListOf()).apply {
                add(AgendaEventItem(it.date, it))
            }
        }

        calgendaDataMap.values.forEach { it ->
            it.sortBy { it.date }
        }

        showCalgendaData()
    }

    private fun showCalgendaData() {
        val today = Calendar.getInstance().time

        //construct agenda list data model
        val agendaDataList: MutableList<AgendaBaseItem> = mutableListOf()
        val agendaDateIndexMap: TreeMap<String, Int> = TreeMap()
        var index = 0
        calgendaDataMap.entries.forEach { entry ->
            entry.key.getDate(CALGENDA_DATE_FORMAT)?.let {
                val item = AgendaDayItem(it, today.compare(it) == 0)
                agendaDataList.add(item)
                agendaDateIndexMap[entry.key] = index
                index += if (entry.value.isEmpty()) { // put empty event item
                    agendaDataList.add(AgendaEmptyEventItem(it))
                    2
                } else {
                    agendaDataList.addAll(entry.value)
                    entry.value.size + 1
                }
            }
        }

        val calendarDataList: MutableList<CalendarItem> = mutableListOf()
        val calendarDateIndexMap: TreeMap<String, Int> = TreeMap()
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        calgendaDataMap.entries.forEachIndexed { index, entry ->
            entry.key.getDate(CALGENDA_DATE_FORMAT)?.let {
                val isToday = today.compare(it) == 0
                val alternation = (it.get(Calendar.MONTH) - currentMonth) % 2 == 0
                val item = CalendarItem(it,
                    alternation,
                    entry.value as List<AgendaEventItem<Any>>,
                    isToday,
                    isToday)
                calendarDataList.add(item)
                calendarDataList.sortBy { it.date }
                calendarDateIndexMap[entry.key] = index
            }
        }

        agenda_view.onDataChange(agendaDateIndexMap, agendaDataList)
        calendar_view.onDataChange(calendarDateIndexMap, calendarDataList)
    }

    interface OnCalgendaListener {

        fun onMonthChange(newMonth: Date)
    }
}