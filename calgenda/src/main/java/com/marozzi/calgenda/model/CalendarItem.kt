package com.marozzi.calgenda.model

import com.marozzi.calgenda.util.CALGENDA_DATE_FORMAT
import com.marozzi.calgenda.util.formatDate
import java.util.*

/**
 * @author by amarozzi on 2019-11-04
 */
data class CalendarItem(
    /**
     * Date of the calendar item
     */
    var date: Date,
    /**
     * True if the month is alternation of the current month (month - currentMonth) % 2 == 0, useful for change the background color
     */
    var isAlternation: Boolean = false,
    /**
     * True if selected
     */
    var isSelected: Boolean = false,
    /**
     * True if is today
     */
    var isToday: Boolean = false) {

    /**
     * List of agenda event item set for the date
     */
    val agendaEvents: MutableSet<AgendaBaseItem> = mutableSetOf()

    fun getDateAsString(): String = date.formatDate(CALGENDA_DATE_FORMAT)
}