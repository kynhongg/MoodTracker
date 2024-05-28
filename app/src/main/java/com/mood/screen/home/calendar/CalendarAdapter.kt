package com.mood.screen.home.calendar

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.mood.R
import com.mood.base.BaseAdapterRecyclerView
import com.mood.databinding.ItemDayCalendarHomeBinding
import com.mood.utils.hide
import com.mood.utils.showOrGone
import kotlin.math.min

class CalendarAdapter : BaseAdapterRecyclerView<CalendarEntity, ItemDayCalendarHomeBinding>() {
    override fun inflateBinding(inflater: LayoutInflater, parent: ViewGroup): ItemDayCalendarHomeBinding {
        return ItemDayCalendarHomeBinding.inflate(inflater, parent, false)
    }

    override fun bindData(binding: ItemDayCalendarHomeBinding, item: CalendarEntity, position: Int) {
        binding.imgBeanType.setImageResource(item.beanIcon)
        if (!item.isShow) {
            binding.root.hide()
            binding.root.isEnabled = false
            return
        }
        binding.tvWeekTitle.apply {
            showOrGone(item.isWeekTitle)
            text = item.weekTitle
        }
        binding.layoutBean.showOrGone(!item.isWeekTitle)
        var alpha = 1.0f
        var textColor = R.color.text_color_54
        if (item.isToday) {
            textColor = R.color.text_color
        }
        if (item.isFeature) {
            textColor = R.color.text_color_10
            alpha = 0.3f
        }
        if (item.isSelected && !item.isFeature) {
            textColor = R.color.green_stroke
        }
        binding.tvBeanDay.apply {
            text = item.value
            setTextColor(ContextCompat.getColor(binding.root.context, textColor))
        }
        binding.imgBeanType.alpha = alpha
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setDataCalendar(data: List<CalendarEntity>, onDone: () -> Unit) {
        if (dataList.isEmpty()) {
            dataList.addAll(data)
            notifyDataSetChanged()
            onDone.invoke()
            return
        }

        val oldSize = dataList.size
        val newSize = data.size
        val compareSize = min(oldSize, newSize)
        val changedIndices = mutableListOf<Int>()

        for (index in 0 until compareSize) {
            if (!dataList[index].compare(data[index])) {
                dataList[index] = data[index]
                changedIndices.add(index)
            }
        }

        if (newSize < oldSize) {
            for (index in oldSize - 1 downTo newSize) {
                dataList.removeAt(index)
                notifyItemRemoved(index)
            }
        } else if (newSize > oldSize) {
            for (index in oldSize until newSize) {
                dataList.add(data[index])
                notifyItemInserted(index)
            }
        }

        if (changedIndices.isNotEmpty()) {
            changedIndices.forEach { notifyItemChanged(it) }
        }
        onDone.invoke()
    }

    fun setSelectedItem(position: Int) {
        if (!dataList[position].isFeature) {
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

    fun clearSelected() {
        val indexSelected = dataList.indexOfFirst { it.isSelected }
        if (indexSelected != -1) {
            dataList[indexSelected].isSelected = false
            notifyItemChanged(indexSelected)
        }
    }

    fun getSelectedItem() = dataList.find { it.isSelected }
}