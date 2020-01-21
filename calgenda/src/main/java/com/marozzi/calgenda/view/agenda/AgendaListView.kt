package com.marozzi.calgenda.view.agenda

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.marozzi.calgenda.adapter.AgendaListRecyclerAdapter
import com.marozzi.calgenda.adapter.AgendaViewHandler
import com.marozzi.calgenda.model.AgendaBaseItem
import com.marozzi.calgenda.model.AgendaDayItem
import com.marozzi.calgenda.model.AgendaEventItem
import com.marozzi.calgenda.util.AppExecutors
import com.marozzi.calgenda.util.CALGENDA_DATE_FORMAT
import com.marozzi.calgenda.util.CALGENDA_DATE_FORMAT_MONTH
import com.marozzi.calgenda.util.formatDate
import java.util.*
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.forEach
import kotlin.collections.mutableListOf
import kotlin.collections.set

/**
 * Created by amarozzi on 2019-11-08
 */
internal class AgendaListView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : BaseAgendaView(context, attrs, defStyleAttr) {

    private val recyclerView = RecyclerView(context)
    private val adapter: AgendaListRecyclerAdapter
    private val layoutManager: LinearLayoutManager

    private val header = FrameLayout(context)
    private var stickyHeaderHeight: Int = 0
    private var isAgendaScrollTriggerByCalendar = false
    private var currentItemPosition = 0

    private var currentMoth: String = ""

    private val items = Collections.synchronizedMap(TreeMap<String, MutableList<AgendaBaseItem>>())
    private val itemsIndex = Collections.synchronizedMap(TreeMap<String, Int>())

    init {
        addView(recyclerView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        addView(header, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        recyclerView.let { it ->
            it.adapter = AgendaListRecyclerAdapter(context).also {
                adapter = it
            }
            it.layoutManager = LinearLayoutManager(context).also {
                layoutManager = it
            }
            it.setHasFixedSize(false)
            it.addOnScrollListener(object : RecyclerView.OnScrollListener() {

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        listener?.onScrollChange()
                    }
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val isScrollUp = dy >= 0
                    header.visibility = View.VISIBLE
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                    if (adapter.getItemViewType(currentItemPosition + 1) == AgendaBaseItem.AGENDA_ITEM_TYPE_DAY) {
                        layoutManager.findViewByPosition(currentItemPosition + 1)?.let {
                            header.y = if (it.top <= stickyHeaderHeight) {
                                (-(stickyHeaderHeight - it.top)).toFloat()
                            } else {
                                0f
                            }
                        }
                    }
                    if (currentItemPosition != firstVisibleItemPosition) {
                        currentItemPosition = firstVisibleItemPosition
                        header.y = 0f
                        handleStickyHeaderUIChange(currentItemPosition, isScrollUp)
                    }
                }
            })
        }
    }

    private fun handleStickyHeaderUIChange(position: Int, isScrollUp: Boolean) {
        var item = adapter.agendaItemList[position]
        if (!isScrollUp) { //scroll down -> show the day info before the current date
            val index = itemsIndex[item.date.formatDate("yyyyMMdd")] ?: 0
            item = adapter.agendaItemList[index]
        }
        if (item is AgendaDayItem) {
            if (!isAgendaScrollTriggerByCalendar) {
                adapter.agendaViewHandler?.bindAgendaDayHeader(item, header.tag as RecyclerView.ViewHolder)
                if (isScrollUp) { // when user scroll up, change agenda item scroll action again
                    moveToDate(item.date)
                }
                listener?.onDateChange(item.date)
                val month = item.date.formatDate(CALGENDA_DATE_FORMAT_MONTH)
                if (month != currentMoth) {
                    currentMoth = month
                    listener?.onMonthChange(item.date)
                }
            }
        }
    }

    override fun moveToDate(date: Date) {
        isAgendaScrollTriggerByCalendar = true
        header.visibility = View.INVISIBLE
        itemsIndex[date.formatDate(CALGENDA_DATE_FORMAT)]?.let { pos ->
            recyclerView.post {
                layoutManager.scrollToPositionWithOffset(pos, 0)
            }
            isAgendaScrollTriggerByCalendar = false
        }
    }

    override fun init(startDate: Date, endDate: Date, callback: () -> Unit) {
        AppExecutors.background().execute {
            items.clear()
            itemsIndex.clear()
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
                val item = AgendaDayItem(startDateSanitized.time, startDateSanitized == today)
                items[startDateSanitized.time.formatDate(CALGENDA_DATE_FORMAT)] = mutableListOf<AgendaBaseItem>(item)
                itemsIndex[startDateSanitized.time.formatDate(CALGENDA_DATE_FORMAT)] = index++
                startDateSanitized.add(Calendar.DAY_OF_MONTH, 1)
            }
            AppExecutors.mainThread().execute { callback.invoke() }
        }
    }

    override fun onDataChange(agendaDataList: List<AgendaEventItem>, callback: () -> Unit) {
        AppExecutors.background().execute {
            synchronized(items) {
                items.forEach {
                    val each = it.value.iterator()
                    while (each.hasNext()) {
                        if (each.next() !is AgendaDayItem) {
                            each.remove()
                        }
                    }
                }
                itemsIndex.clear()

                agendaDataList.forEach { item ->
                    items[item.getDateAsString()]?.add(item)
                }

                var index = 0
                val addData = mutableListOf<AgendaBaseItem>()
                items.forEach {
                    itemsIndex[it.key] = index
                    index += it.value.size
                    addData.addAll(it.value)
                }
                AppExecutors.mainThread().execute {
                    recyclerView.post { adapter.updateAgendaList(addData) }
                    callback.invoke()
                }
            }
        }
    }

    override fun setAgendaViewHandler(agendaViewHandler: AgendaViewHandler) {
        adapter.agendaViewHandler = agendaViewHandler
        val customHeader = agendaViewHandler.getAgendaDayHeaderHolder(LayoutInflater.from(context), header)
        header.tag = customHeader
        header.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                header.viewTreeObserver.removeOnGlobalLayoutListener(this)
                stickyHeaderHeight = header.height
                header.visibility = View.INVISIBLE
            }
        })
        header.addView(customHeader.itemView)
    }
}