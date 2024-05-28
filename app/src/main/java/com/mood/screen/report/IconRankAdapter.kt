package com.mood.screen.report

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.mood.R
import com.mood.base.BaseAdapterRecyclerView
import com.mood.databinding.LayoutItemRankingBinding
import com.mood.utils.getDrawableIdByName

class IconRankAdapter : BaseAdapterRecyclerView<IconRankEntity, LayoutItemRankingBinding>() {
    override fun inflateBinding(inflater: LayoutInflater, parent: ViewGroup): LayoutItemRankingBinding {
        return LayoutItemRankingBinding.inflate(inflater, parent, false)
    }

    override fun bindData(binding: LayoutItemRankingBinding, item: IconRankEntity, position: Int) {
        val context = binding.root.context
        binding.tvOrder.text = item.order.toString()
        binding.tvIconName.text = item.iconName
        binding.imgIcon.setImageResource(context.getDrawableIdByName(item.iconUrl))
        binding.tvCountIcon.text = item.iconCount.toString()
        binding.tvCountDiff.text = item.iconDiff.toString()
        val textColorRed = ContextCompat.getColor(context, R.color.tart_orange)
        val textColorGreen = ContextCompat.getColor(context, R.color.ufo_green)
        binding.imgIconCompare.setImageResource(
            when (item.iconCompareType) {
                IconRank.Top -> {
                    binding.tvCountDiff.setTextColor(textColorGreen)
                    R.drawable.ic_icon_top
                }

                IconRank.Down -> {
                    binding.tvCountDiff.setTextColor(textColorRed)
                    R.drawable.ic_icon_down
                }

                else -> {
                    binding.tvCountDiff.text = "  "
                    R.drawable.ic_icon_equal
                }
            }
        )
    }
}