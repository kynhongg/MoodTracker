package com.mood.screen.report

data class IconRankEntity(
    val order: Int,
    val iconName: String,
    val iconUrl: String,
    val iconCount: Int,
    val iconDiff: Int,
    val iconCompareType: IconRank = IconRank.Equal
)
