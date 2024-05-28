package com.mood.screen.setting.content.entity

data class IconBase(
    val name: String,
    val sourceId: Int,
    var sourceIdOff: Int = 0,
    var iconId: Int = -1,
    var isSelected: Boolean = false
)