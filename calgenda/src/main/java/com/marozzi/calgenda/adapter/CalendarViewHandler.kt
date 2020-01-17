package com.marozzi.calgenda.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.marozzi.calgenda.model.CalendarItem

/**
 * Created by amarozzi on 2019-11-04
 */

interface CalendarViewHandler {

    fun getCalendarViewHolder(layoutInflater: LayoutInflater, parent: ViewGroup): RecyclerView.ViewHolder

    fun bindCalendarView(item: CalendarItem, holder: RecyclerView.ViewHolder)

}