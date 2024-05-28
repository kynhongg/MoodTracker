package com.mood.screen.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.mood.R
import com.mood.base.BaseAdapterRecyclerView
import com.mood.databinding.ItemTimeDialogBinding
import com.mood.utils.CalendarUtil

class ChooseTimeAdapter : BaseAdapterRecyclerView<ChooseTimeEntity, ItemTimeDialogBinding>() {
    override fun inflateBinding(inflater: LayoutInflater, parent: ViewGroup): ItemTimeDialogBinding {
        return ItemTimeDialogBinding.inflate(inflater, parent, false)
    }

    override fun bindData(binding: ItemTimeDialogBinding, item: ChooseTimeEntity, position: Int) {
        val context = binding.root.context
        if (item.isSelected) {
            binding.tvItemTime.setTextColor(ContextCompat.getColor(context, R.color.white))
            binding.root.setBackgroundResource(R.drawable.bg_radius_item_choose_time_selected)
        } else {
            val currentYear = CalendarUtil.getYearInt()
            if (item.year > currentYear || (item.year == currentYear && CalendarUtil.isFuture(context, item.month))) {
                binding.tvItemTime.setTextColor(ContextCompat.getColor(context, R.color.text_color_25))
            } else {
                binding.tvItemTime.setTextColor(ContextCompat.getColor(context, R.color.text_color))
            }
            binding.root.setBackgroundResource(R.drawable.bg_radius_item_choose_time)
        }
        if (item.isSelectMonth) {
            binding.tvItemTime.text = item.month
        } else {
            binding.tvItemTime.text = item.year.toString()
        }
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

    fun updateYearAllData(year: Int) {
        dataList.forEach { it.year = year }
        notifyItemRangeChanged(0, dataList.size)
    }

    fun resetSelect() {
        val indexSelected = dataList.indexOfFirst { it.isSelected }
        if (indexSelected != -1) {
            dataList[indexSelected].isSelected = false
            notifyItemChanged(indexSelected)
        }
    }
}