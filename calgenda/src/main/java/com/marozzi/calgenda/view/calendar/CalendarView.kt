package com.marozzi.calgenda.view.calendar

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.marozzi.calgenda.adapter.CalendarGridViewAdapter
import com.marozzi.calgenda.adapter.CalendarViewHandler
import com.marozzi.calgenda.model.AgendaBaseItem
import com.marozzi.calgenda.model.CalendarItem
import com.marozzi.calgenda.util.*
import java.util.*


/**
 * Created by amarozzi on 2019-11-08
 */
internal class CalendarView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {

    var listener: OnCalendarViewListener? = null

    private val recyclerView: RecyclerView = RecyclerView(context)
    private val layoutManager: GridLayoutManager = GridLayoutManager(context, 7)
    private val adapter: CalendarGridViewAdapter = CalendarGridViewAdapter(context) {
        listener?.onCalendarItemSelected(it)
    }

    private val cellItemHeight: Int = context.resources.displayMetrics.widthPixels / 7
    private var calendarMinRow: Int = 1
    private var calendarMaxRow: Int = 5
    private val calendarMinHeight: Int = cellItemHeight * calendarMinRow
    private val calendarMaxHeight: Int = cellItemHeight * calendarMaxRow

    private var currentMonth: String = ""
    private var calendarIndex = Collections.synchronizedSortedMap(TreeMap<String, Int>())
    private var calendarItems = Collections.synchronizedList(mutableListOf<CalendarItem>())

    var status = CalendarViewStatus.COLLAPSE
        private set

    /**
     * Set if a drag operation on the Calendar View opens the Grid or not. True to open, false otherwise
     */
    var dragToOpen = false

    enum class CalendarViewStatus {
        EXPAND, COLLAPSE
    }

    init {
        addView(recyclerView)

        recyclerView.let { it ->
            it.layoutManager = layoutManager
            it.adapter = adapter.also {
                adapter.cellItemSize = cellItemHeight
            }
            it.setHasFixedSize(true)
            it.addOnScrollListener(object : RecyclerView.OnScrollListener() {

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    when (newState) {
                        RecyclerView.SCROLL_STATE_DRAGGING -> {
                            if (dragToOpen) changeStatus(CalendarViewStatus.EXPAND)
                        }
                        RecyclerView.SCROLL_STATE_IDLE -> {
                            val fistPos = layoutManager.findFirstVisibleItemPosition()
                            val lastPos = layoutManager.findLastVisibleItemPosition() + 1
                            val monthMap = mutableMapOf<String, Int>()
                            for (i in fistPos..lastPos) {
                                adapter.getItem(i)?.let {
                                    val format = it.date.formatDate(CALGENDA_DATE_FORMAT_MONTH)
                                    monthMap[format] = (monthMap[format] ?: 0) + 1
                                }
                            }

                            var monthMajor: Map.Entry<String, Int> = monthMap.entries.first()
                            monthMap.entries.forEach {
                                if (it.value > monthMajor.value) monthMajor = it
                            }
                            if (currentMonth != monthMajor.key) {
                                currentMonth = monthMajor.key
                                listener?.onMonthChange(monthMajor.key.getDate(CALGENDA_DATE_FORMAT_MONTH)!!)
                            }
                        }
                    }
                }
            })
            it.setOnTouchListener { _, _ ->
                if (status == CalendarViewStatus.EXPAND) false else !dragToOpen
            }
            PagerSnapWithSpanCountHelper(32).attachToRecyclerView(it)
        }

        val layoutParams = recyclerView.layoutParams
        layoutParams.height = calendarMinHeight
        recyclerView.layoutParams = layoutParams
    }

    fun init(startDate: Date, endDate: Date, callback: () -> Unit) {
        AppExecutors.background().execute {
            calendarItems.clear()
            calendarIndex.clear()
            val startDateSanitized = Calendar.getInstance().apply {
                time = startDate
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val endDateSanitized = Calendar.getInstance().apply {
                time = endDate
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val today = Calendar.getInstance().apply {
                time = startDate
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            var index = 0
            while (startDateSanitized <= endDateSanitized) {
                val isToday = today == startDateSanitized
                val alternation = (startDateSanitized.get(Calendar.MONTH) - today.get(Calendar.MONTH)) % 2 == 0
                calendarItems.add(CalendarItem(startDateSanitized.time, alternation, isToday, isToday))
                calendarIndex[startDateSanitized.time.formatDate(CALGENDA_DATE_FORMAT)] = index++
                startDateSanitized.add(Calendar.DAY_OF_MONTH, 1)
            }
            AppExecutors.mainThread().execute { callback.invoke() }
        }
    }

    fun onDataChange(newData: List<AgendaBaseItem>, callback: () -> Unit) {
        AppExecutors.background().execute {
            synchronized(calendarItems) {
                calendarItems.forEach {
                    it.agendaEvents.clear()
                }
                newData.forEach { agenda ->
                    calendarIndex[agenda.getDateAsString()]?.let {
                        calendarItems[it].agendaEvents.add(agenda)
                    }
                }
                AppExecutors.mainThread().execute {
                    post { adapter.setItems(calendarItems) }
                    callback.invoke()
                }
            }
        }
    }

    fun setCalendarViewHandler(calendarViewHandler: CalendarViewHandler) {
        adapter.calendarViewHandler = calendarViewHandler
    }

    fun changeStatus(status: CalendarViewStatus) {
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE || this.status == status) {
            return  //do not change calendar view height when rotate to landscape
        }
        this.status = status
        val height = if (status == CalendarViewStatus.EXPAND) calendarMaxHeight
        else calendarMinHeight
        ValueAnimator.ofInt(recyclerView.measuredHeight, height).apply {
            addUpdateListener { valueAnimator ->
                val layoutParams = recyclerView.layoutParams
                layoutParams.height = valueAnimator.animatedValue as Int
                recyclerView.layoutParams = layoutParams
            }
            //FIXME when finish listener move to select item            addListener()
            duration = 500
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    fun scrollToDateByPosition(position: Int) {
        adapter.setItemSelected(position)
        recyclerView.post { recyclerView.scrollToPosition(position) }
    }

    fun scrollToDate(date: Date) {
        scrollToDateByPosition(calendarIndex[date.formatDate(CALGENDA_DATE_FORMAT)] ?: 0)
    }

    fun getFirstCalendarItemVisible(): CalendarItem? = adapter.getItem(layoutManager.findFirstVisibleItemPosition())

    interface OnCalendarViewListener {
        /**
         * When the user select a date on the calendar view
         */
        fun onCalendarItemSelected(calendarItem: CalendarItem)

        /**
         * when the user scroll the calendar view and the month change
         */
        fun onMonthChange(date: Date)
    }

}