package com.marozzi.calgenda.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.marozzi.calgenda.model.CalendarItem


/**
 * @author by amarozzi on 2019-11-04
 */
class CalendarGridViewAdapter(context: Context, var listener: ((item: CalendarItem) -> Unit)? = null) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var cellItemSize = 0

    var items: List<CalendarItem> = emptyList()
        private set

    var calendarViewHandler: CalendarViewHandler? = null

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        requireNotNull(calendarViewHandler) { "CalendarViewHandler is null" }
        val holder = calendarViewHandler!!.getCalendarViewHolder(layoutInflater, parent)

        // set item size
        val layoutParams = ViewGroup.LayoutParams(cellItemSize, cellItemSize)
        holder.itemView.layoutParams = layoutParams

        return holder
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        requireNotNull(calendarViewHandler) { "CalendarViewHandler is null" }

        val item = items[position]
        val holder = viewHolder as CalendarViewHolder

        calendarViewHandler!!.bindCalendarView(item, holder)

        holder.itemView.setOnClickListener {
            setItemSelected(position)
            listener?.invoke(item)
        }
    }

    override fun getItemCount(): Int {
        return if (items.isEmpty()) 0 else items.size
    }

    fun setItems(calendarItemList: List<CalendarItem>) {
        this.items = calendarItemList
        notifyDataSetChanged()
    }

    fun setItemSelected(position: Int) {
        if (position < 0 || position >= items.size) {
            return
        }
        items.forEachIndexed { index, calendarItem ->
            calendarItem.isSelected = index == position
        }
        notifyDataSetChanged()
    }

    fun getItem(position: Int): CalendarItem? {
        return if (position < items.size) items[position] else null
    }
}