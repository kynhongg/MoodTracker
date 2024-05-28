package com.mood.screen.home.calendar

import android.view.LayoutInflater
import android.view.ViewGroup
import com.mood.base.BaseAdapterRecyclerView
import com.mood.data.entity.IconEntity
import com.mood.databinding.LayoutItemIconBeanBinding
import com.mood.utils.getDrawableIdByName

class BeanIconAdapter : BaseAdapterRecyclerView<IconEntity, LayoutItemIconBeanBinding>() {
    override fun inflateBinding(inflater: LayoutInflater, parent: ViewGroup): LayoutItemIconBeanBinding {
        return LayoutItemIconBeanBinding.inflate(inflater, parent, false)
    }

    override fun bindData(binding: LayoutItemIconBeanBinding, item: IconEntity, position: Int) {
        binding.imgIconBean.setImageResource(binding.root.context.getDrawableIdByName(item.iconUrl ?: ""))
    }
}