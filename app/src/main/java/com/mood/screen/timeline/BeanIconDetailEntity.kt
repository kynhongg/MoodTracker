package com.mood.screen.timeline

import com.mood.data.entity.BeanDailyEntity
import com.mood.data.entity.BeanImageAttachEntity
import com.mood.data.entity.IconEntity

data class BeanIconDetailEntity(
    val beanDailyEntity: BeanDailyEntity,
    val listIcon: List<IconEntity>,
    val listImageAttach: List<BeanImageAttachEntity>,
    var isAds: Boolean = false
)