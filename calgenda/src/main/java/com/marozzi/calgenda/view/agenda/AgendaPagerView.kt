package com.marozzi.calgenda.view.agenda

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.marozzi.calgenda.adapter.AgendaPageRecyclerAdapter
import com.marozzi.calgenda.adapter.AgendaViewHandler
import com.marozzi.calgenda.model.*
import com.marozzi.calgenda.util.CALGENDA_DATE_FORMAT
import com.marozzi.calgenda.util.CALGENDA_DATE_FORMAT_MONTH
import com.marozzi.calgenda.util.formatDate
import java.util.*

/**
 * Created by amarozzi on 2019-11-08
 */
internal class AgendaPagerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : BaseAgendaView(context, attrs, defStyleAttr) {

    private val viewPager = ViewPager2(context)
    private val adapter: AgendaPageRecyclerAdapter
    private val pageItems = mutableListOf<AgendaPageItem>()

    private var isAgendaScrollTriggerByCalendar = false

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
        isAgendaScrollTriggerByCalendar = true
        val dateFormat = date.formatDate(CALGENDA_DATE_FORMAT)
        val pos = pageItems.indexOfFirst { it.dayItem.date.formatDate(CALGENDA_DATE_FORMAT) == dateFormat }
        viewPager.setCurrentItem(pos, false)
        isAgendaScrollTriggerByCalendar = false
    }

    override fun setAgendaViewHandler(agendaViewHandler: AgendaViewHandler) {
        adapter.agendaViewHandler = agendaViewHandler
    }

    override fun onDataChange(agendaDateIndexMap: TreeMap<String, Int>, agendaDataList: MutableList<AgendaBaseItem>) {
        pageItems.clear()
        agendaDataList.forEach {
            when (it) {
                is AgendaDayItem -> {
                    pageItems.add(AgendaPageItem(it))
                }
                is AgendaEmptyEventItem, is AgendaEventItem -> {
                    pageItems.find { page ->
                        page.dayItem.date == it.date
                    }?.events?.add(it)
                }
            }
        }

        adapter.updateAgendaList(pageItems)
    }

}