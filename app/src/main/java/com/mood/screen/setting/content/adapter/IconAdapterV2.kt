package com.mood.screen.setting.content.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.mood.R
import com.mood.base.BaseAdapterRecyclerView
import com.mood.data.entity.IconEntity
import com.mood.databinding.ItemTypeBodyBlockIconBinding
import com.mood.utils.getDrawableIdByName
import com.mood.utils.setOnSafeClick

class IconAdapterV2 : BaseAdapterRecyclerView<IconEntity, ItemTypeBodyBlockIconBinding>() {
    private var onClickIcon: ((item: IconEntity, position: Int) -> Unit)? = null

    fun setOnClickIcon(listener: (item: IconEntity, position: Int) -> Unit) {
        onClickIcon = listener
    }

    override fun inflateBinding(inflater: LayoutInflater, parent: ViewGroup): ItemTypeBodyBlockIconBinding {
        return ItemTypeBodyBlockIconBinding.inflate(inflater, parent, false)
    }

    override fun bindData(binding: ItemTypeBodyBlockIconBinding, item: IconEntity, position: Int) {
        val context = binding.root.context
        if (!item.isTemp) {
            binding.tvIconName.text = item.iconName
            if (item.iconIsShow) {
                binding.tvIconName.setTextColor(ContextCompat.getColor(context, R.color.text_color))
            } else {
                binding.tvIconName.setTextColor(ContextCompat.getColor(context, R.color.text_color_25))
            }
        } else {
            binding.tvIconName.text = context.getString(R.string.add)
            binding.tvIconName.setTextColor(ContextCompat.getColor(context, R.color.green_stroke))
        }
        val iconName = if (item.iconIsShow) {
            item.iconUrl ?: "R.drawable.ic_bean_type_default"
        } else {
            item.iconUrl + "_off"
        }
        binding.imgIconBlock.apply {
            if (!item.isTemp) {
                setImageResource(context.getDrawableIdByName((iconName)))
            } else {
                setImageResource(context.getDrawableIdByName(("ic_add_icon")))
            }
            setOnSafeClick {
                onClickIcon?.invoke(item, position)
            }
        }
    }

    fun setSelectedItem(position: Int) {
        val isSelected = dataList[position].isSelected
        dataList[position].isSelected = !isSelected
        notifyItemChanged(position)
    }
}