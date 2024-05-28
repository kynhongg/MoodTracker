package com.mood.screen.addbean

import android.view.LayoutInflater
import android.view.ViewGroup
import com.mood.base.BaseAdapterRecyclerView
import com.mood.data.entity.IconEntity
import com.mood.databinding.ItemTypeBodyBlockIconBinding
import com.mood.utils.getDrawableIdByName
import com.mood.utils.setOnSafeClick

class IconAdapter : BaseAdapterRecyclerView<IconEntity, ItemTypeBodyBlockIconBinding>() {
    private var onClickIcon: ((item: IconEntity, position: Int) -> Unit)? = null

    fun setOnClickIcon(listener: (item: IconEntity, position: Int) -> Unit) {
        onClickIcon = listener
    }

    override fun inflateBinding(inflater: LayoutInflater, parent: ViewGroup): ItemTypeBodyBlockIconBinding {
        return ItemTypeBodyBlockIconBinding.inflate(inflater, parent, false)
    }

    override fun bindData(binding: ItemTypeBodyBlockIconBinding, item: IconEntity, position: Int) {
        binding.tvIconName.text = item.iconName
        val context = binding.root.context
        val iconName = if (item.isSelected) {
            item.iconUrl ?: "R.drawable.ic_bean_type_default"
        } else {
            item.iconUrl + "_off"
        }
        binding.imgIconBlock.apply {
            setImageResource(context.getDrawableIdByName((iconName)))
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