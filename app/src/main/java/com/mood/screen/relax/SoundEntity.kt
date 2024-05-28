package com.mood.screen.relax

data class SoundEntity(
    val name: String,
    val imageSource: Int,
    val url: String,
    val isMute: Boolean,
    var isSelected: Boolean = false
)
