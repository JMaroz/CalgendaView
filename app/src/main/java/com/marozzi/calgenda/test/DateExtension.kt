package com.marozzi.calgenda.test

import java.text.SimpleDateFormat
import java.util.*

fun Date.formatDate(pattern: String): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(this)
}

fun Date.get(field: Int): Int {
    val cal = Calendar.getInstance()
    cal.timeInMillis = this.time
    return cal.get(field)
}