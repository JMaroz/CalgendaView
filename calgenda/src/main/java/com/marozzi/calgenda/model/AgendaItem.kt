package com.marozzi.calgenda.model

import com.marozzi.calgenda.util.CALGENDA_DATE_FORMAT
import com.marozzi.calgenda.util.formatDate
import java.util.*

/**
 * Created by amarozzi on 2019-11-04
 */
interface AgendaBaseItem {

    companion object {
        var AGENDA_ITEM_TYPE_DAY = 0
        var AGENDA_ITEM_TYPE_EVENT = 2
    }

    val date: Date
    val type: Int
    val dateString:String
}

data class AgendaDayItem(override val date: Date, var isToday: Boolean) : AgendaBaseItem {

    override val type: Int = AgendaBaseItem.AGENDA_ITEM_TYPE_DAY
    override val dateString: String = date.formatDate(CALGENDA_DATE_FORMAT)
}

data class AgendaEventItem(val event: Event, override val date: Date = event.date, var isFirst: Boolean, var isLast: Boolean) : AgendaBaseItem {

    override val type: Int = AgendaBaseItem.AGENDA_ITEM_TYPE_EVENT
    override val dateString: String = date.formatDate(CALGENDA_DATE_FORMAT)

    // leave equals and hasCode

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

internal data class AgendaPageItem(val dayItem: AgendaDayItem) {

    val events = mutableListOf<AgendaEventItem>()

}