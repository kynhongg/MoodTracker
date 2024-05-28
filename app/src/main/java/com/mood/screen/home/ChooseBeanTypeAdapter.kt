package com.mood.screen.home

import android.view.LayoutInflater
import android.view.ViewGroup
import com.mood.base.BaseAdapterRecyclerView
import com.mood.data.entity.BeanDefaultEmoji
import com.mood.databinding.ItemLayoutChooseBeanBinding

class ChooseBeanTypeAdapter : BaseAdapterRecyclerView<BeanDefaultEmoji, ItemLayoutChooseBeanBinding>() {
    override fun inflateBinding(inflater: LayoutInflater, parent: ViewGroup): ItemLayoutChooseBeanBinding {
        return ItemLayoutChooseBeanBinding.inflate(inflater, parent, false)
    }

    override fun bindData(binding: ItemLayoutChooseBeanBinding, item: BeanDefaultEmoji, position: Int) {
        binding.imgTypeBean.setImageResource(item.icon)
    }
}