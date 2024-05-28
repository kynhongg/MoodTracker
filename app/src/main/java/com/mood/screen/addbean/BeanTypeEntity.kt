package com.mood.screen.addbean

import com.mood.data.entity.BeanDefaultEmoji

data class BeanTypeEntity(
    var beanDefaultEmoji: BeanDefaultEmoji,
    var isSelected: Boolean = false
)
