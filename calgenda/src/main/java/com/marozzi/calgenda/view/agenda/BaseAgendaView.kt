package com.marozzi.calgenda.view.agenda

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.marozzi.calgenda.adapter.AgendaViewHandler
import com.marozzi.calgenda.model.AgendaEventItem
import java.util.*

/**
 * Created by amarozzi on 2020-01-17
 */
internal abstract class BaseAgendaView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {

    var listener: OnAgendaViewListener? = null

    abstract fun init(startDate: Date, endDate: Date, callback: () -> Unit)

    abstract fun setAgendaViewHandler(agendaViewHandler: AgendaViewHandler)

    abstract fun moveToDate(date: Date)

    abstract fun onDataChange(agendaDataList: List<AgendaEventItem>, callback: () -> Unit)

    interface OnAgendaViewListener {

        fun onScrollChange()

        fun onDateChange(date: Date)

        fun onMonthChange(date: Date)

    }
}