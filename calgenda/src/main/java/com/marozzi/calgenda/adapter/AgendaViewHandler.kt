package com.marozzi.calgenda.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.marozzi.calgenda.model.AgendaDayItem
import com.marozzi.calgenda.model.AgendaEmptyEventItem
import com.marozzi.calgenda.model.AgendaEventItem

/**
 * Created by amarozzi on 2019-11-04
 */
interface AgendaViewHandler {

    fun getAgendaDayHeaderHolder(layoutInflater: LayoutInflater, parent: ViewGroup): AgendaDayHeaderHolder

    fun getAgendaEmptyEventHolder(layoutInflater: LayoutInflater, parent: ViewGroup): AgendaEmptyEventHolder

    fun getAgendaEventHolder(layoutInflater: LayoutInflater, parent: ViewGroup): AgendaEventHolder

    fun bindAgendaDayHeader(dayItem: AgendaDayItem, holder: AgendaDayHeaderHolder)

    fun bindAgendaEmptyEvent(emptyEvent: AgendaEmptyEventItem, holder: AgendaEmptyEventHolder)

    fun bindAgendaEvent(event: AgendaEventItem, holder: AgendaEventHolder)

}

abstract class AgendaDayHeaderHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

abstract class AgendaEmptyEventHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

abstract class AgendaEventHolder(itemView: View) : RecyclerView.ViewHolder(itemView)