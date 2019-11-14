package com.marozzi.calgenda.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange

import java.util.*

/**
 * Created by amarozzi on 2019-11-04
 */
internal class WeekView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(
    context,
    attrs,
    defStyleAttr) {

    private var textSize = 11
    private var weekdaysColor = Color.LTGRAY
    private var weekendColor = Color.GRAY
    private var startDay = Calendar.MONDAY
    private var unSelectedAlpha = 1f
    private val textViews = mutableListOf<TextView>()

    init {
        orientation = HORIZONTAL
        initWeek(startDay)
    }

    fun setCustomizations(@ColorInt backgroundColor: Int, @ColorInt weekdaysColor: Int, @ColorInt weekendColor: Int, textSize: Int, @FloatRange(from = 0.0, to = 1.0) unSelectedAlpha:Float) {
        setBackgroundColor(backgroundColor)
        this.unSelectedAlpha = unSelectedAlpha
        this.weekdaysColor = weekdaysColor
        this.weekendColor = weekendColor
        this.textSize = textSize
        initWeek(startDay)
    }

    fun initWeek(startDay: Int) {
        this.startDay = startDay
        removeAllViews()
        textViews.clear()

        val calendar = Calendar.getInstance().apply {
            firstDayOfWeek = startDay
            set(Calendar.DAY_OF_WEEK, startDay)
        }

        for (i in 0 until 7) {
            val textView = TextView(context)
            textView.layoutParams = LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
            textView.text = calendar.getDisplayName(Calendar.DAY_OF_WEEK,
                Calendar.SHORT,
                Locale.getDefault())
            textView.textSize = textSize.toFloat()
            textView.tag = calendar.get(Calendar.DAY_OF_WEEK)

            val weekend = calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || calendar.get(
                Calendar.DAY_OF_WEEK) == Calendar.SUNDAY

            textView.setTextColor(if (weekend) weekendColor else weekdaysColor)

            textView.gravity = Gravity.CENTER
            addView(textView)
            textViews.add(textView)

            calendar.add(Calendar.DAY_OF_WEEK, 1)
        }
    }

    fun setCurrentSelectedDay(day:Int) {
        textViews.forEach {
            if ((it.tag as Int) == day) {
                it.alpha = 1f
            } else {
                it.alpha = unSelectedAlpha
            }
        }
    }
}
