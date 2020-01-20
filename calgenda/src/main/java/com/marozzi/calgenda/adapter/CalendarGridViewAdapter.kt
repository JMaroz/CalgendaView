package com.marozzi.calgenda.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.marozzi.calgenda.model.CalendarItem


/**
 * @author by amarozzi on 2019-11-04
 */
internal class CalendarGridViewAdapter(context: Context, var listener: ((item: CalendarItem) -> Unit)? = null) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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
        val holder = viewHolder

        calendarViewHandler!!.bindCalendarView(item, holder)

        holder.itemView.setOnClickListener {
            setItemSelected(position)
            listener?.invoke(item)
        }
    }

    override fun getItemCount(): Int {
        return items.size
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

    private class MyDiffCallback(private val oldList: List<CalendarItem>, private val newList: List<CalendarItem>) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].getDateAsString() === newList[newItemPosition].getDateAsString()
        }

        override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
            val name = oldList[oldPosition]
            val name1 = newList[newPosition]

            return name.agendaEvents== name1.agendaEvents
        }
    }
}