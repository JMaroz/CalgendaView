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
import com.marozzi.calgenda.view.agenda.AgendaListView
import com.marozzi.calgenda.view.agenda.AgendaPagerView
import com.marozzi.calgenda.view.agenda.BaseAgendaView
import com.marozzi.calgenda.view.calendar.CalendarView
import kotlinx.android.synthetic.main.calgenda_view.view.*
import java.util.*


/**
 * Created by amarozzi on 2019-11-04
 */
class CalgendaView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {

    private val baseAgendaView: BaseAgendaView

    /**
     * Map where for every day has a list of agenda items
     */
    private var dataMap: SortedMap<String, MutableSet<AgendaEventItem>> = Collections.synchronizedSortedMap(TreeMap<String, MutableSet<AgendaEventItem>>())

    var listener: OnCalgendaListener? = null

    var currentDate: Date? = null
        private set

    /**
     * If the user change the month by hand we will not move to the current date if events changes
     */
    private var changedByUserClick = false

    private var startDate: Date = Date()
    private var endDate: Date = Date()

    private var agendaInit = false
    private var calendarInit = false

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

        agenda_view.setBackgroundColor(a.getColor(R.styleable.CalgendaView_cg_agenda_background_color, Color.WHITE))

        val agendaType = a.getInt(R.styleable.CalgendaView_cg_agenda_type, 0)
        baseAgendaView = if (agendaType == 0) AgendaListView(context) else AgendaPagerView(context)
        agenda_view.addView(baseAgendaView)
        a.recycle()

        baseAgendaView.listener = object : BaseAgendaView.OnAgendaViewListener {

            override fun onMonthChange(date: Date) {
                changedByUserClick = false
                listener?.onMonthChange(date)
            }

            override fun onScrollChange() {
                calendar_view.changeStatus(CalendarView.CalendarViewStatus.COLLAPSE)
            }

            override fun onDateChange(date: Date) {
                currentDate = date
                calendar_view.postDelayed({
                    calendar_week_headbar_view.setCurrentSelectedDay(date.get(Calendar.DAY_OF_WEEK))
                    calendar_view.scrollToDate(date)
                }, 200)
            }
        }

        calendar_view.listener = object : CalendarView.OnCalendarViewListener {

            override fun onMonthChange(date: Date) {
                changedByUserClick = true
                calendar_week_headbar_view.setCurrentSelectedDay(date.get(Calendar.DAY_OF_WEEK))
                listener?.onMonthChange(date)
            }

            override fun onCalendarItemSelected(calendarItem: CalendarItem) {
                currentDate = calendarItem.date
                baseAgendaView.moveToDate(calendarItem.date)
            }
        }
    }

    fun setHeaderCustomizations(@ColorInt headerBackgroundColor: Int, @ColorInt headerWeekdaysColor: Int, @ColorInt headerWeekendColor: Int, @Dimension headerTextSize: Int, @FloatRange(from = 0.0, to = 1.0) headerUnSelectedAlpha: Float) {
        calendar_week_headbar_view.setCustomizations(headerBackgroundColor, headerWeekdaysColor, headerWeekendColor, headerTextSize, headerUnSelectedAlpha)
    }

    fun initCalgenda(calendarViewHandler: CalendarViewHandler, agendaViewHandler: AgendaViewHandler, startDate: Date, endDate: Date, startWeekDay: Int, events: List<Event> = emptyList()) {
        calendar_week_headbar_view.initWeek(startWeekDay)

        calendar_view.setCalendarViewHandler(calendarViewHandler)
        baseAgendaView.setAgendaViewHandler(agendaViewHandler)

        AppExecutors.background().execute {
            //init agenda date list, data 7(col) * 65(row) = 455 (cell)
            dataMap.clear()

            val todayCalendar1 = Calendar.getInstance().apply {
                firstDayOfWeek = startWeekDay
            }
            val todayCalendar2 = Calendar.getInstance().apply {
                firstDayOfWeek = startWeekDay
            }

            //init current week days
            val todayOfWeek = todayCalendar1.get(Calendar.DAY_OF_WEEK)
            for (i in todayOfWeek downTo startWeekDay) {
                dataMap[todayCalendar1.time.formatDate(CALGENDA_DATE_FORMAT)] = mutableSetOf()
                todayCalendar1.add(Calendar.DAY_OF_MONTH, -1)
            }
            todayCalendar2.add(Calendar.DAY_OF_MONTH, 1) //tomorrow
            for (i in todayOfWeek + 1..Calendar.SATURDAY + 1) {
                dataMap[todayCalendar2.time.formatDate(CALGENDA_DATE_FORMAT)] = mutableSetOf()
                todayCalendar2.add(Calendar.DAY_OF_MONTH, 1)
            }

            //init row before the current week days
            var min = startDate.daysBetween(todayCalendar1.time)
            while (min % 7 != 0) {
                min++
            }
            val beforeSize = min
            for (i in 0 until beforeSize) {
                dataMap[todayCalendar1.time.formatDate(CALGENDA_DATE_FORMAT)] = mutableSetOf()
                todayCalendar1.add(Calendar.DAY_OF_MONTH, -1)
            }

            //init row after the current week days
            var max = todayCalendar2.time.daysBetween(endDate)
            while (max % 7 != 0) {
                max++
            }
            val afterSize = max
            for (i in 0 until afterSize) {
                dataMap[todayCalendar2.time.formatDate(CALGENDA_DATE_FORMAT)] = mutableSetOf()
                todayCalendar2.add(Calendar.DAY_OF_MONTH, 1)
            }

            this@CalgendaView.startDate = dataMap.keys.first().getDate(CALGENDA_DATE_FORMAT)!!
            this@CalgendaView.endDate = dataMap.keys.last().getDate(CALGENDA_DATE_FORMAT)!!

            currentDate = Calendar.getInstance().time
            AppExecutors.mainThread().execute {
                fun check() {
                    if (isInit()) addEvents(events, true)
                    listener?.onInitDone()
                }
                baseAgendaView.init(this@CalgendaView.startDate, this@CalgendaView.endDate) {
                    agendaInit = true
                    check()
                }
                calendar_view.init(this@CalgendaView.startDate, this@CalgendaView.endDate) {
                    calendarInit = true
                    check()
                }
            }
        }
    }

    fun isInit(): Boolean = agendaInit && calendarInit

    fun toggleCalendar() {
        calendar_view.changeStatus(if (calendar_view.status == CalendarView.CalendarViewStatus.COLLAPSE) {
            CalendarView.CalendarViewStatus.EXPAND
        } else {
            CalendarView.CalendarViewStatus.COLLAPSE
        })
    }

    fun setEvents(events: List<Event>, moveToCurrentDay: Boolean) {
        require(isInit()) { "Unable to set events before Calgenda is init" }
        dataMap.values.forEach { it.clear() } // clear all the events for every day
        addEvents(events, moveToCurrentDay)
    }

    fun addEvents(events: List<Event>, moveToCurrentDay: Boolean) {
        require(isInit()) { "Unable to add events before Calgenda is init" }
        AppExecutors.background().execute {
            synchronized(dataMap) {
                events.forEach { event ->
                    val date = event.date.formatDate(CALGENDA_DATE_FORMAT)
                    if (event.date in startDate..endDate) {
                        dataMap[date] = (dataMap[date] ?: mutableSetOf()).apply {
                            add(AgendaEventItem(event, isFirst = false, isLast = false))
                        }
                    }
                }

                val allData = mutableListOf<AgendaEventItem>()
                dataMap.entries.forEach {
                    it.value.forEachIndexed { index, agendaEventItem ->
                        agendaEventItem.isFirst = index == 0
                        agendaEventItem.isLast = index == it.value.size - 1
                    }
                    allData.addAll(it.value)
                }

                AppExecutors.mainThread().execute {
                    var agendaFinish = false
                    var calendarFinish = false
                    fun check() {
                        if (agendaFinish && calendarFinish && !changedByUserClick && moveToCurrentDay) {
                            currentDate?.let {
                                baseAgendaView.moveToDate(it)
                                calendar_view.scrollToDate(it)
                                calendar_week_headbar_view.setCurrentSelectedDay(it.get(Calendar.DAY_OF_WEEK))
                            }
                        }
                    }
                    baseAgendaView.onDataChange(allData.toList()) {
                        agendaFinish = true
                        check()
                    }
                    calendar_view.onDataChange(allData.toList()) {
                        calendarFinish = true
                        check()
                    }
                }
            }
        }
    }

    interface OnCalgendaListener {

        fun onInitDone()

        fun onMonthChange(newMonth: Date)
    }
}