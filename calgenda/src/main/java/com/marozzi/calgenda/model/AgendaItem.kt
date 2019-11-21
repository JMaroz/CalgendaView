package com.marozzi.calgenda.model

import java.util.*

/**
 * Created by amarozzi on 2019-11-04
 */
interface AgendaBaseItem {

    companion object {
        var AGENDA_ITEM_TYPE_DAY = 0
        var AGENDA_ITEM_TYPE_EMPTY_EVENT = 1
        var AGENDA_ITEM_TYPE_EVENT = 2
    }

    val date: Date
    val type: Int

}

data class AgendaDayItem(override val date: Date, var isToday: Boolean) : AgendaBaseItem {

    override val type: Int = AgendaBaseItem.AGENDA_ITEM_TYPE_DAY
}

data class AgendaEmptyEventItem(override var date: Date) : AgendaBaseItem {

    override var type: Int = AgendaBaseItem.AGENDA_ITEM_TYPE_EMPTY_EVENT
}

class AgendaEventItem(val event: Event, override val date: Date = event.date, var isFirst: Boolean, var isLast: Boolean) : AgendaBaseItem {

    override val type: Int = AgendaBaseItem.AGENDA_ITEM_TYPE_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AgendaEventItem

        if (event != other.event) return false

        return true
    }

    override fun hashCode(): Int {
        return event.hashCode()
    }

}