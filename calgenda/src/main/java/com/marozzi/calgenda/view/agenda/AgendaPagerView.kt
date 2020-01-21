package com.marozzi.calgenda.view.agenda

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.marozzi.calgenda.adapter.AgendaPageRecyclerAdapter
import com.marozzi.calgenda.adapter.AgendaViewHandler
import com.marozzi.calgenda.model.*
import com.marozzi.calgenda.util.AppExecutors
import com.marozzi.calgenda.util.CALGENDA_DATE_FORMAT
import com.marozzi.calgenda.util.CALGENDA_DATE_FORMAT_MONTH
import com.marozzi.calgenda.util.formatDate
import java.util.*
import kotlin.collections.HashMap

/**
 * Created by amarozzi on 2019-11-08
 */
internal class AgendaPagerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : BaseAgendaView(context, attrs, defStyleAttr) {

    private val viewPager = ViewPager2(context)
    private val adapter: AgendaPageRecyclerAdapter

    private val pageItems = Collections.synchronizedList(mutableListOf<AgendaPageItem>())
    private val pageItemsIndex = Collections.synchronizedMap(TreeMap<String, Int>())

    private var currentMoth: String = ""

    init {
        addView(viewPager, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        viewPager.let { it ->
            it.adapter = AgendaPageRecyclerAdapter(context).also {
                adapter = it
            }
            it.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

                override fun onPageScrollStateChanged(state: Int) {
                    if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                        listener?.onScrollChange()
                    }
                }

                override fun onPageSelected(position: Int) {
                    val date = pageItems[position].dayItem.date
                    listener?.onDateChange(date)
                    val month = date.formatDate(CALGENDA_DATE_FORMAT_MONTH)
                    if (month != currentMoth) {
                        currentMoth = month
                        listener?.onMonthChange(date)
                    }
                }
            })
        }
    }

    override fun moveToDate(date: Date) {
        val dateFormat = date.formatDate(CALGENDA_DATE_FORMAT)
        Log.d("AgendaPagerView", dateFormat)
        pageItemsIndex[dateFormat]?.let {
            viewPager.post { viewPager.setCurrentItem(it, false) }
        }
    }

    override fun init(startDate: Date, endDate: Date, callback: () -> Unit) {
        AppExecutors.background().execute {
            pageItems.clear()
            pageItemsIndex.clear()
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
                pageItems.add(AgendaPageItem(AgendaDayItem(startDateSanitized.time, startDateSanitized == today)))
                pageItemsIndex[startDateSanitized.time.formatDate(CALGENDA_DATE_FORMAT)] = index++
                startDateSanitized.add(Calendar.DAY_OF_MONTH, 1)
            }
            AppExecutors.mainThread().execute { callback.invoke() }
        }
    }

    override fun setAgendaViewHandler(agendaViewHandler: AgendaViewHandler) {
        adapter.agendaViewHandler = agendaViewHandler
    }

    override fun onDataChange(agendaDataList: List<AgendaEventItem>, callback: () -> Unit) {
        AppExecutors.background().execute {
            synchronized(pageItems) {
                //delete old events
                pageItems.forEach {
                    it.events.clear()
                }
                //add new events
                agendaDataList.forEach {
                    pageItemsIndex[it.getDateAsString()]?.let { index ->
                        pageItems[index]?.events?.add(it)
                    }
                }
                AppExecutors.mainThread().execute {
                    viewPager.post { adapter.updateAgendaList(pageItems) }
                    callback.invoke()
                }
            }
        }
    }

}