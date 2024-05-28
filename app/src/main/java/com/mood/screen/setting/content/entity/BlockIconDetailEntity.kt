package com.mood.screen.setting.content.entity

import com.mood.data.entity.BlockEmojiEntity
import com.mood.data.entity.IconEntity

data class BlockIconDetailEntity(
    val blockEmojiEntity: BlockEmojiEntity,
    val listIcon: List<IconEntity>,
    var isVisibility: Boolean = false
)