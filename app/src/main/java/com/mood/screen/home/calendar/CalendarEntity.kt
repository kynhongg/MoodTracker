package com.mood.screen.home.calendar

import com.mood.R

data class CalendarEntity(
    var weekTitle: String = "",
    var beanIcon: Int = R.drawable.ic_bean_type_default,
    var value: String,
    var day: Int,
    var month: Int,
    var year: Int,
    var isWeekTitle: Boolean = false,
    var isToday: Boolean = false,
    var isFeature: Boolean = false,
    var isShow: Boolean = true,
    var isSelected: Boolean = false
) {
    fun compare(other: CalendarEntity): Boolean {
        return other.weekTitle == weekTitle &&
                other.beanIcon == beanIcon &&
                other.value == value &&
                other.day == day &&
                other.month == month &&
                other.year == year &&
                other.isWeekTitle == isWeekTitle &&
                other.isToday == isToday &&
                other.isFeature == isFeature &&
                other.isShow == isShow
    }

    fun updateNewValue(newEntity: CalendarEntity) {
        weekTitle = newEntity.weekTitle
        beanIcon = newEntity.beanIcon
        value = newEntity.value
        day = newEntity.day
        month = newEntity.month
        year = newEntity.year
        isWeekTitle = newEntity.isWeekTitle
        isToday = newEntity.isToday
        isFeature = newEntity.isFeature
        isShow = newEntity.isShow
    }
}
