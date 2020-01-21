package com.marozzi.calgenda.test

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.marozzi.calgenda.adapter.AgendaViewHandler
import com.marozzi.calgenda.model.AgendaDayItem
import com.marozzi.calgenda.model.AgendaEventItem
import kotlinx.android.synthetic.main.calgenda_item_date_header.view.*
import kotlinx.android.synthetic.main.calgenda_item_event.view.*
import java.util.*

/**
 * Created by amarozzi on 2019-11-04
 */
class AgendaViewHandlerImp : AgendaViewHandler {

    override fun getAgendaDayHeaderHolder(layoutInflater: LayoutInflater, parent: ViewGroup): RecyclerView.ViewHolder = AgendaDayHeaderHolderImp(layoutInflater.inflate(R.layout.calgenda_item_date_header, parent, false))

    override fun getAgendaEmptyEventHolder(layoutInflater: LayoutInflater, parent: ViewGroup): RecyclerView.ViewHolder = AgendaEmptyEventHolderImp(layoutInflater.inflate(R.layout.calgenda_item_empty_event, parent, false))

    override fun getAgendaEventHolder(layoutInflater: LayoutInflater, parent: ViewGroup): RecyclerView.ViewHolder = AgendaEventHolderImp(layoutInflater.inflate(R.layout.calgenda_item_event, parent, false))

    override fun bindAgendaDayHeader(dayItem: AgendaDayItem, events: List<AgendaEventItem>, holder: RecyclerView.ViewHolder) {
        val viewHolder = holder as AgendaDayHeaderHolderImp

        viewHolder.date.text = dayItem.date.formatDate("EEE, dd MMM yyyy").toUpperCase(Locale.getDefault())

        viewHolder.itemView.setOnClickListener {
            Toast.makeText(it.context, dayItem.date.formatDate("EEE, MMM dd"), Toast.LENGTH_SHORT).show()
        }
    }

    override fun bindAgendaEmptyEvent(holder: RecyclerView.ViewHolder) {
        val viewHolder = holder as AgendaEmptyEventHolderImp
        viewHolder.itemView.setOnClickListener {
            Toast.makeText(it.context, "No event in this day", Toast.LENGTH_SHORT).show()
        }
    }

    override fun bindAgendaEvent(event: AgendaEventItem, holder: RecyclerView.ViewHolder) {
        val viewHolder = holder as AgendaEventHolderImp
        if (event.isLast) {
            viewHolder.divider.visibility = View.GONE
        } else {
            viewHolder.divider.visibility = View.VISIBLE
        }
        viewHolder.event.text = (event.event as MainActivity.MockEvent).id
        viewHolder.itemView.setOnClickListener {
            Toast.makeText(it.context, "Clicked on event", Toast.LENGTH_SHORT).show()
        }
    }
}

class AgendaDayHeaderHolderImp(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val date: TextView = itemView.date
}

class AgendaEmptyEventHolderImp(itemView: View) : RecyclerView.ViewHolder(itemView)

class AgendaEventHolderImp(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val event: TextView = itemView.event
    val divider: View = itemView.divider
}