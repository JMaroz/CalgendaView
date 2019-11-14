package com.marozzi.calgenda.util

import java.text.SimpleDateFormat
import java.util.*

internal const val CALGENDA_DATE_FORMAT = "yyyyMMdd"
internal const val CALGENDA_DATE_FORMAT_MONTH = "yyyyMM"

internal fun String.getDate(pattern: String): Date? {
    return try {
        SimpleDateFormat(pattern, Locale.getDefault()).parse(this)
    } catch (e: Exception) {
        null
    }
}

internal fun Date.formatDate(pattern: String): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(this)
}

internal fun Date.add(field: Int, amount: Int) {
    val cal = Calendar.getInstance()
    cal.timeInMillis = this.time
    cal.add(field, amount)

    this.time = cal.time.time
}

internal fun Date.get(field: Int): Int {
    val cal = Calendar.getInstance()
    cal.timeInMillis = this.time
    return cal.get(field)
}

internal fun Date.daysBetween(d2: Date): Int {
    return ((d2.time - time) / (1000 * 60 * 60 * 24)).toInt()
}

/**
 *
 * @param date1
 * @param date2
 * @return 0 same date, -N data2 major of data1, +N data1 minus of data2
 */
internal fun Date.compare(date2: Date): Int {
    val c1 = GregorianCalendar()
    c1.time = this
    val c2 = GregorianCalendar()
    c2.time = date2
    return c1.compare(c2)
}

/**
 *
 * @param c1
 * @param c2
 * @return 0 same date, -N c2 major of c1, +N c1 minus of c2
 */
internal fun Calendar.compare(c2: Calendar): Int {
    if (get(Calendar.YEAR) != c2.get(Calendar.YEAR)) return get(Calendar.YEAR) - c2.get(Calendar.YEAR)
    return if (get(Calendar.MONTH) != c2.get(Calendar.MONTH)) get(Calendar.MONTH) - c2.get(Calendar.MONTH) else get(
        Calendar.DAY_OF_MONTH) - c2.get(Calendar.DAY_OF_MONTH)
}