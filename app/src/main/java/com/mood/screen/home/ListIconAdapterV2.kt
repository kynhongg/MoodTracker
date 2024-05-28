package com.mood.screen.home

import android.view.LayoutInflater
import android.view.ViewGroup
import com.mood.base.BaseAdapterRecyclerView
import com.mood.databinding.LayoutSourceIconBinding
import com.mood.screen.setting.content.entity.IconBase

class ListIconAdapterV2 : BaseAdapterRecyclerView<IconBase, LayoutSourceIconBinding>() {
    override fun inflateBinding(inflater: LayoutInflater, parent: ViewGroup): LayoutSourceIconBinding {
        return LayoutSourceIconBinding.inflate(inflater, parent, false)
    }

    override fun bindData(binding: LayoutSourceIconBinding, item: IconBase, position: Int) {
        binding.imgIconBean.setImageResource(
            if (item.isSelected) {
                item.sourceId
            } else {
                item.sourceIdOff
            }
        )
    }

    fun setSelectedItem(position: Int) {
        val index = dataList.indexOfLast { it.isSelected }
        if (index != -1) {
            dataList[index].isSelected = !dataList[index].isSelected
            notifyItemChanged(index)
        }
        dataList[position].isSelected = !dataList[position].isSelected
        notifyItemChanged(position)
    }
}