package com.marozzi.calgenda.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.marozzi.calgenda.model.AgendaDayItem
import com.marozzi.calgenda.model.AgendaEventItem

/**
 * Created by amarozzi on 2019-11-04
 */
interface AgendaViewHandler {

    fun getAgendaDayHeaderHolder(layoutInflater: LayoutInflater, parent: ViewGroup): RecyclerView.ViewHolder

    fun getAgendaEmptyEventHolder(layoutInflater: LayoutInflater, parent: ViewGroup): RecyclerView.ViewHolder

    fun getAgendaEventHolder(layoutInflater: LayoutInflater, parent: ViewGroup): RecyclerView.ViewHolder

    fun bindAgendaDayHeader(dayItem: AgendaDayItem, holder: RecyclerView.ViewHolder)

    fun bindAgendaEmptyEvent(holder: RecyclerView.ViewHolder)

    fun bindAgendaEvent(event: AgendaEventItem, holder: RecyclerView.ViewHolder)

}