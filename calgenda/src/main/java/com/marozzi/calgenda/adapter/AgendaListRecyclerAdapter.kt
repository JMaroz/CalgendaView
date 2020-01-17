package com.marozzi.calgenda.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView

import com.marozzi.calgenda.model.AgendaBaseItem
import com.marozzi.calgenda.model.AgendaDayItem
import com.marozzi.calgenda.model.AgendaEmptyEventItem
import com.marozzi.calgenda.model.AgendaEventItem

/**
 * @author by amarozzi on 2019-11-04
 */
internal class AgendaListRecyclerAdapter(context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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
        val item = agendaItemList[position]
        when (item.type) {
            AgendaBaseItem.AGENDA_ITEM_TYPE_DAY -> agendaViewHandler!!.bindAgendaDayHeader(item as AgendaDayItem, viewHolder)
            AgendaBaseItem.AGENDA_ITEM_TYPE_EVENT -> agendaViewHandler!!.bindAgendaEvent(item as AgendaEventItem, viewHolder)
            else -> agendaViewHandler!!.bindAgendaEmptyEvent(item as AgendaEmptyEventItem, viewHolder)
        }
    }


    override fun getItemViewType(position: Int): Int {
        return agendaItemList[position].type
    }

    override fun getItemCount(): Int {
        return agendaItemList.size
    }

    /**
     * update list
     */
    fun updateAgendaList(agendaItemList: List<AgendaBaseItem>) {
        this.agendaItemList = agendaItemList
        notifyDataSetChanged()
    }
}