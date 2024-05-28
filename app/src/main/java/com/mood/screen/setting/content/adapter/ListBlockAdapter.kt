package com.mood.screen.setting.content.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.mood.R
import com.mood.base.BaseAdapterRecyclerView
import com.mood.data.entity.BlockEmojiEntity
import com.mood.databinding.LayoutItemBlockBinding

class ListBlockAdapter : BaseAdapterRecyclerView<BlockEmojiEntity, LayoutItemBlockBinding>() {
    override fun inflateBinding(inflater: LayoutInflater, parent: ViewGroup): LayoutItemBlockBinding {
        return LayoutItemBlockBinding.inflate(inflater, parent, false)
    }

    override fun bindData(binding: LayoutItemBlockBinding, item: BlockEmojiEntity, position: Int) {
        binding.radioCheck.setImageResource(
            if (item.isSelected) {
                R.drawable.ic_block_radio_group_select
            } else {
                R.drawable.ic_block_radio_group
            }
        )
        binding.tvBlockName.text = item.blockName
    }

    fun setSelectedItem(index: Int) {
        if (index !in 0 until dataList.size) {
            return
        }
        val indexSelected = dataList.indexOfFirst { it.isSelected }
        if (indexSelected != -1) {
            dataList[indexSelected].isSelected = false
            notifyItemChanged(indexSelected)
        }
        dataList[index].isSelected = true
        notifyItemChanged(index)
    }
}