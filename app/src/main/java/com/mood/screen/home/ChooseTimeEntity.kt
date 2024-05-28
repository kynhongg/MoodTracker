package com.mood.screen.home

data class ChooseTimeEntity(
    val month: String,
    var year: Int,
    var isSelectMonth: Boolean = true,
    var isSelected: Boolean = false,
    val monthInt: Int = 0,
)