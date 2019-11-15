package com.marozzi.calgenda.test

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.marozzi.calgenda.adapter.AgendaDayHeaderHolder
import com.marozzi.calgenda.adapter.AgendaEmptyEventHolder
import com.marozzi.calgenda.adapter.AgendaEventHolder
import com.marozzi.calgenda.adapter.AgendaViewHandler
import com.marozzi.calgenda.model.AgendaDayItem
import com.marozzi.calgenda.model.AgendaEmptyEventItem
import com.marozzi.calgenda.model.AgendaEventItem
import java.util.*

/**
 * Created by amarozzi on 2019-11-04
 */
class AgendaViewHandlerImp : AgendaViewHandler {

    override fun getAgendaDayHeaderHolder(layoutInflater: LayoutInflater, parent: ViewGroup): AgendaDayHeaderHolder = AgendaDayHeaderHolderImp(
        layoutInflater.inflate(R.layout.calgenda_item_date_header, parent, false))

    override fun getAgendaEmptyEventHolder(layoutInflater: LayoutInflater, parent: ViewGroup): AgendaEmptyEventHolder = AgendaEmptyEventHolderImp(
        layoutInflater.inflate(R.layout.calgenda_item_empty_event, parent, false))

    override fun getAgendaEventHolder(layoutInflater: LayoutInflater, parent: ViewGroup): AgendaEventHolder = AgendaEventHolderImp(
        layoutInflater.inflate(R.layout.calgenda_item_event, parent, false))

    override fun bindAgendaDayHeader(dayItem: AgendaDayItem, holder: AgendaDayHeaderHolder) {
        val viewHolder = holder as AgendaDayHeaderHolderImp

        viewHolder.date.text = dayItem.date.formatDate("EEE, dd MMM yyyy")
            .toUpperCase(Locale.getDefault())

        viewHolder.itemView.setOnClickListener {
            Toast.makeText(it.context, dayItem.date.formatDate("EEE, MMM dd"), Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun bindAgendaEmptyEvent(emptyEvent: AgendaEmptyEventItem, holder: AgendaEmptyEventHolder) {
        val viewHolder = holder as AgendaEmptyEventHolderImp
        viewHolder.itemView.setOnClickListener {
            Toast.makeText(it.context, "No event in this day", Toast.LENGTH_SHORT).show()
        }
    }

    override fun bindAgendaEvent(event: AgendaEventItem, holder: AgendaEventHolder) {
        val viewHolder = holder as AgendaEventHolderImp
        viewHolder.itemView.setOnClickListener {
            Toast.makeText(it.context, "Clicked on event", Toast.LENGTH_SHORT).show()
        }
    }
}

class AgendaDayHeaderHolderImp(itemView: View) : AgendaDayHeaderHolder(itemView) {
    val date: TextView = itemView.findViewById(R.id.date)
}

class AgendaEmptyEventHolderImp(itemView: View) : AgendaEmptyEventHolder(itemView)

class AgendaEventHolderImp(itemView: View) : AgendaEventHolder(itemView) {
}