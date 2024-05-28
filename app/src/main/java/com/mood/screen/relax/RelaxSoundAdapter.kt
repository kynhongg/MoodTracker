package com.mood.screen.relax

import android.view.LayoutInflater
import android.view.ViewGroup
import com.mood.R
import com.mood.base.BaseAdapterRecyclerView
import com.mood.databinding.ItemTriggerSoundBinding
import com.mood.utils.SharePrefUtils
import com.mood.utils.showOrGone

class RelaxSoundAdapter : BaseAdapterRecyclerView<SoundEntity, ItemTriggerSoundBinding>() {
    override fun inflateBinding(inflater: LayoutInflater, parent: ViewGroup): ItemTriggerSoundBinding {
        return ItemTriggerSoundBinding.inflate(inflater, parent, false)
    }

    override fun bindData(binding: ItemTriggerSoundBinding, item: SoundEntity, position: Int) {
        val stringMute = binding.root.context.getString(R.string.mute)
        binding.tvSoundName.text = item.name
        binding.imgSound.setImageResource(item.imageSource)
        binding.layoutSound.setBackgroundResource(
            if (item.isSelected) {
                if (item.name == stringMute) {
                    binding.imgSound.setImageResource(R.drawable.mute_select)
                }
                R.drawable.background_select_sound
            } else {
                if (item.name == stringMute) {
                    binding.imgSound.setImageResource(R.drawable.mute)
                }
                R.drawable.background_default_sound
            }
        )
        binding.icPremium.showOrGone(
            position > 2 && position < dataList.lastIndex
                    && !SharePrefUtils.isBought()
        )
    }

    fun setSelectedItem(index: Int) {
        if (index !in 0 until dataList.size) {
            return
        }
        val indexSelected = dataList.indexOfFirst { it.isSelected }
        if (index == indexSelected) {
            dataList[index].isSelected = !dataList[index].isSelected
            notifyItemChanged(index)
        } else {
            if (indexSelected != -1) {
                dataList[indexSelected].isSelected = false
                notifyItemChanged(indexSelected)
            }
            dataList[index].isSelected = true
            notifyItemChanged(index)
        }
    }

    fun isSelectSound() = dataList.any { it.isSelected }
    fun getIndexSelect() = dataList.indexOfFirst { it.isSelected }
}