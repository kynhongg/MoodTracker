package com.mood.screen.addbean

import android.view.LayoutInflater
import android.view.ViewGroup
import com.mood.base.BaseAdapterRecyclerView
import com.mood.databinding.ItemLayoutChooseBeanBinding

class BeanTypeAdapter : BaseAdapterRecyclerView<BeanTypeEntity, ItemLayoutChooseBeanBinding>() {
    override fun inflateBinding(inflater: LayoutInflater, parent: ViewGroup): ItemLayoutChooseBeanBinding {
        return ItemLayoutChooseBeanBinding.inflate(inflater, parent, false)
    }

    override fun bindData(binding: ItemLayoutChooseBeanBinding, item: BeanTypeEntity, position: Int) {
        binding.imgTypeBean.setImageResource(
            if (item.isSelected) {
                item.beanDefaultEmoji.icon
            } else {
                item.beanDefaultEmoji.iconOff
            }
        )
    }

    fun setSelectedItem(position: Int) {
        val indexSelected = dataList.indexOfFirst { it.isSelected }
        if (indexSelected != -1) {
            dataList[indexSelected].isSelected = false
            notifyItemChanged(indexSelected)
        }
        if (position == -1) {
            return
        }
        dataList[position].isSelected = true
        notifyItemChanged(position)
    }
}