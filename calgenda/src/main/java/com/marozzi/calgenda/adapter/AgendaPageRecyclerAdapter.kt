package com.marozzi.calgenda.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.marozzi.calgenda.model.*

/**
 * @author by amarozzi on 2019-11-04
 */
internal class AgendaPageRecyclerAdapter(context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var agendaItemList: List<AgendaPageItem> = mutableListOf()
        private set

    var agendaViewHandler: AgendaViewHandler? = null

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        requireNotNull(agendaViewHandler) { "AgendaViewHandler is null" }
        return AgendaPageViewHolder(parent.context)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        requireNotNull(agendaViewHandler) { "AgendaViewHandler is null" }
        val item = agendaItemList[position]
        val group = viewHolder.itemView as LinearLayout
        group.removeAllViews()

        val dayViewHolder = agendaViewHandler!!.getAgendaDayHeaderHolder(layoutInflater, group)
        group.addView(dayViewHolder.itemView)
        agendaViewHandler!!.bindAgendaDayHeader(item.dayItem, dayViewHolder)

        item.events.forEach { baseItem ->
            when (baseItem.type) {
                AgendaBaseItem.AGENDA_ITEM_TYPE_EVENT -> {
                    val eventViewHolder = agendaViewHandler!!.getAgendaEventHolder(layoutInflater, group)
                    group.addView(eventViewHolder.itemView)
                    agendaViewHandler!!.bindAgendaEvent(baseItem as AgendaEventItem, eventViewHolder)
                }
                AgendaBaseItem.AGENDA_ITEM_TYPE_EMPTY_EVENT -> {
                    val emptyEventViewHolder = agendaViewHandler!!.getAgendaEmptyEventHolder(layoutInflater, group)
                    group.addView(emptyEventViewHolder.itemView)
                    agendaViewHandler!!.bindAgendaEmptyEvent(baseItem as AgendaEmptyEventItem, emptyEventViewHolder)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return agendaItemList.size
    }

    /**
     * update list
     */
    fun updateAgendaList(agendaItemList: List<AgendaPageItem>) {
        this.agendaItemList = agendaItemList
        notifyDataSetChanged()
    }

    private class AgendaPageViewHolder(context: Context) : RecyclerView.ViewHolder(LinearLayout(context).apply {
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        orientation = LinearLayout.VERTICAL
    })
}