package com.marozzi.calgenda.test

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.marozzi.calgenda.adapter.CalendarViewHandler
import com.marozzi.calgenda.model.AgendaEventItem
import com.marozzi.calgenda.model.CalendarItem
import java.util.*

/**
 * Created by amarozzi on 2019-11-04
 */

class CalendarViewHandlerImp : CalendarViewHandler {

    private val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    override fun getCalendarViewHolder(layoutInflater: LayoutInflater, parent: ViewGroup): RecyclerView.ViewHolder = CalendarViewHolderImp(
        layoutInflater.inflate(R.layout.calgenda_item_day, parent, false))

    override fun bindCalendarView(item: CalendarItem, holder: RecyclerView.ViewHolder) {
        val viewHolder = holder as CalendarViewHolderImp

        viewHolder.day.text = item.date.get(Calendar.DAY_OF_MONTH).toString()

        if (item.date.get(Calendar.DAY_OF_MONTH) == 1) {
            viewHolder.month.visibility = View.VISIBLE
            viewHolder.month.text = item.date.formatDate("MMM")
            val year = item.date.get(Calendar.YEAR)
            if (year != currentYear) {
                viewHolder.year.visibility = View.VISIBLE
                viewHolder.year.text = year.toString()
            } else {
                viewHolder.year.visibility = View.GONE
            }
        } else {
            viewHolder.month.visibility = View.GONE
            viewHolder.year.visibility = View.GONE
        }

        if (item.isToday) {
            viewHolder.month.visibility = View.GONE
            viewHolder.year.visibility = View.GONE
            viewHolder.today.visibility = View.VISIBLE
        } else {
            viewHolder.today.visibility = View.GONE
        }

        if (item.isSelected) {
            viewHolder.day.alpha = 1f
            viewHolder.month.alpha = 1f
            viewHolder.year.alpha = 1f
            viewHolder.today.alpha = 1f
            viewHolder.events.alpha = 1f
        } else {
            viewHolder.day.alpha = .6f
            viewHolder.month.alpha = .6f
            viewHolder.year.alpha = .6f
            viewHolder.today.alpha = .6f
            viewHolder.events.alpha = .6f
        }

        if (item.agendaEvents.isEmpty()) {
            viewHolder.events.visibility = View.GONE
        } else {
            viewHolder.events.visibility = View.VISIBLE
        }
    }

    class CalendarViewHolderImp(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var day: TextView = itemView.findViewById(R.id.item_day)
        var month: TextView = itemView.findViewById(R.id.item_month)
        var year: TextView = itemView.findViewById(R.id.item_year)
        var events: View = itemView.findViewById(R.id.item_events)
        var today: View = itemView.findViewById(R.id.item_today)
    }
}