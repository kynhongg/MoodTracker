package com.mood.screen.setting.content.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.mood.base.BaseAdapterRecyclerView
import com.mood.data.entity.IconEntity
import com.mood.databinding.ItemIconSettingBinding
import com.mood.utils.getDrawableIdByName

class SettingIconAdapter : BaseAdapterRecyclerView<IconEntity, ItemIconSettingBinding>() {
    override fun inflateBinding(inflater: LayoutInflater, parent: ViewGroup): ItemIconSettingBinding {
        return ItemIconSettingBinding.inflate(inflater, parent, false)
    }

    override fun bindData(binding: ItemIconSettingBinding, item: IconEntity, position: Int) {
        val context = binding.root.context
        binding.tvIconName.text = item.iconName
        item.iconUrl?.let { context.getDrawableIdByName(it) }?.let { binding.imgIcon.setImageResource(it) }
    }
}