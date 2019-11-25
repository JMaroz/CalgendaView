package com.marozzi.calgenda.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

import com.marozzi.calgenda.model.AgendaBaseItem
import com.marozzi.calgenda.model.AgendaDayItem
import com.marozzi.calgenda.model.AgendaEmptyEventItem
import com.marozzi.calgenda.model.AgendaEventItem

/**
 * @author by amarozzi on 2019-11-04
 */
class AgendaRecyclerAdapter(context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var agendaItemList: List<AgendaBaseItem> = mutableListOf()
        private set

    var agendaViewHandler: AgendaViewHandler? = null

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        requireNotNull(agendaViewHandler) { "AgendaViewHandler is null" }
        return when (viewType) {
            AgendaBaseItem.AGENDA_ITEM_TYPE_DAY -> agendaViewHandler!!.getAgendaDayHeaderHolder(layoutInflater, parent)
            AgendaBaseItem.AGENDA_ITEM_TYPE_EVENT -> agendaViewHandler!!.getAgendaEventHolder(layoutInflater, parent)
            else -> agendaViewHandler!!.getAgendaEmptyEventHolder(layoutInflater, parent)
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        requireNotNull(agendaViewHandler) { "AgendaViewHandler is null" }
        when (viewHolder) {
            is AgendaDayHeaderHolder -> agendaViewHandler!!.bindAgendaDayHeader(agendaItemList[position] as AgendaDayItem, viewHolder)
            is AgendaEventHolder -> agendaViewHandler!!.bindAgendaEvent(agendaItemList[position] as AgendaEventItem, viewHolder)
            is AgendaEmptyEventHolder -> agendaViewHandler!!.bindAgendaEmptyEvent(agendaItemList[position] as AgendaEmptyEventItem, viewHolder)
        }
    }


    override fun getItemViewType(position: Int): Int {
        return agendaItemList[position].type
    }

    override fun getItemCount(): Int {
        return if (agendaItemList.isEmpty()) 0 else agendaItemList.size
    }

    /**
     * update list
     */
    fun updateAgendaList(agendaItemList: List<AgendaBaseItem>) {
        this.agendaItemList = agendaItemList
        notifyDataSetChanged()
    }

}