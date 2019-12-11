package com.marozzi.calgenda.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.FloatRange
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
class CalgendaView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {

    /**
     * Map where for every day has a list of agenda items
     */
    private var calgendaDataMap = TreeMap<String, MutableSet<AgendaEventItem>>()

    var calgendaListener: OnCalgendaListener? = null

    var currentDate: Date? = null
        private set

    /**
     * If the user change the month by hand we will not move to the current date if events changes
     */
    private var changedByUserClick = false

    private val obj = Any()

    init {
        addView(LayoutInflater.from(context).inflate(R.layout.calgenda_view, this, false))

        val a = context.obtainStyledAttributes(attrs, R.styleable.CalgendaView)

        calendar_view.dragToOpen = a.getBoolean(R.styleable.CalgendaView_cg_calendar_drag_to_open, false)

        val headerBackgroundColor = a.getColor(R.styleable.CalgendaView_cg_week_header_background_color, Color.WHITE)
        val headerWeekdaysColor = a.getColor(R.styleable.CalgendaView_cg_week_header_weekdays_color, Color.LTGRAY)
        val headerWeekendColor = a.getColor(R.styleable.CalgendaView_cg_week_header_weekend_color, headerWeekdaysColor)
        val headerTextSize = a.getDimensionPixelSize(R.styleable.CalgendaView_cg_week_header_text_size, resources.getDimensionPixelSize(R.dimen.cg_week_header_text_size))
        val headerUnSelectedAlpha = a.getFloat(R.styleable.CalgendaView_cg_week_header_unselected_alpha, 1f)
        setHeaderCustomizations(headerBackgroundColor, headerWeekdaysColor, headerWeekendColor, headerTextSize, headerUnSelectedAlpha)

        a.recycle()

        agenda_view.listener = object : AgendaView.OnAgendaViewListener {

            override fun onMonthChange(date: Date) {
                changedByUserClick = false
                calgendaListener?.onMonthChange(date)
            }

            override fun onScrollChange() {
                calendar_view.changeStatus(CalendarView.CalendarViewStatus.COLLAPSE)
            }

            override fun onDateChange(date: Date) {
                synchronized(obj) {
                    currentDate = date
                    calendar_view.postDelayed({
                        calendar_week_headbar_view.setCurrentSelectedDay(date.get(Calendar.DAY_OF_WEEK))
                        calendar_view.scrollToDate(date)
                    }, 200)
                }
            }
        }

        calendar_view.listener = object : CalendarView.OnCalendarViewListener {

            override fun onMonthChange(date: Date) {
                changedByUserClick = true
                calendar_week_headbar_view.setCurrentSelectedDay(date.get(Calendar.DAY_OF_WEEK))
                calgendaListener?.onMonthChange(date)
            }

            override fun onCalendarItemSelected(calendarItem: CalendarItem) {
                synchronized(obj) {
                    currentDate = calendarItem.date
                    agenda_view.moveToDate(calendarItem.date)
                }
            }
        }
    }

    fun setHeaderCustomizations(@ColorInt headerBackgroundColor: Int, @ColorInt headerWeekdaysColor: Int, @ColorInt headerWeekendColor: Int, @Dimension headerTextSize: Int, @FloatRange(from = 0.0, to = 1.0) headerUnSelectedAlpha: Float) {
        calendar_week_headbar_view.setCustomizations(headerBackgroundColor, headerWeekdaysColor, headerWeekendColor, headerTextSize, headerUnSelectedAlpha)
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
            calgendaDataMap[todayCalendar1.time.formatDate(CALGENDA_DATE_FORMAT)] = mutableSetOf()
            todayCalendar1.add(Calendar.DAY_OF_MONTH, -1)
        }
        todayCalendar2.add(Calendar.DAY_OF_MONTH, 1) //tomorrow
        for (i in todayOfWeek + 1..Calendar.SATURDAY + 1) {
            calgendaDataMap[todayCalendar2.time.formatDate(CALGENDA_DATE_FORMAT)] = mutableSetOf()
            todayCalendar2.add(Calendar.DAY_OF_MONTH, 1)
        }

        //init row before the current week days
        var min = startDate.daysBetween(todayCalendar1.time)
        while (min % 7 != 0) {
            min++
        }
        val beforeSize = min
        for (i in 0 until beforeSize) {
            calgendaDataMap[todayCalendar1.time.formatDate(CALGENDA_DATE_FORMAT)] = mutableSetOf()
            todayCalendar1.add(Calendar.DAY_OF_MONTH, -1)
        }

        //init row after the current week days
        var max = todayCalendar2.time.daysBetween(endDate)
        while (max % 7 != 0) {
            max++
        }
        val afterSize = max
        for (i in 0 until afterSize) {
            calgendaDataMap[todayCalendar2.time.formatDate(CALGENDA_DATE_FORMAT)] = mutableSetOf()
            todayCalendar2.add(Calendar.DAY_OF_MONTH, 1)
        }

        setEvents(events)

        currentDate = Calendar.getInstance().time
        agenda_view.moveToDate(currentDate!!)
        calendar_view.scrollToDate(currentDate!!)
        calendar_week_headbar_view.setCurrentSelectedDay(currentDate!!.get(Calendar.DAY_OF_WEEK))
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
        addEvents(events, false)
    }

    fun addEvents(events: List<Event>, moveToCurrentDay: Boolean) {
        synchronized(obj) {
            events.forEach { event ->
                val date = event.date.formatDate(CALGENDA_DATE_FORMAT)
                calgendaDataMap[date] = (calgendaDataMap[date] ?: mutableSetOf()).apply {
                    add(AgendaEventItem(event, isFirst = false, isLast = false))
                }
            }

            calgendaDataMap.values.forEach {
                it.forEachIndexed { index, agendaEventItem ->
                    agendaEventItem.isFirst = index == 0
                    agendaEventItem.isLast = index == it.size - 1
                }
            }

            showCalgendaData()

            if (!changedByUserClick && moveToCurrentDay) {
                currentDate?.let {
                    agenda_view.moveToDate(it)
                    calendar_view.scrollToDate(it)
                    calendar_week_headbar_view.setCurrentSelectedDay(it.get(Calendar.DAY_OF_WEEK))
                }
            }
        }
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
                val item = CalendarItem(it, alternation, entry.value, isToday, isToday)
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