package com.mood.screen.setting.app

import android.view.LayoutInflater
import android.view.ViewGroup
import com.mood.R
import com.mood.base.BaseAdapterRecyclerView
import com.mood.databinding.ItemBackgroundImageAppBinding
import com.mood.utils.SharePrefUtils
import com.mood.utils.loadImage
import com.mood.utils.showOrGone

class ImageBackgroundAdapter : BaseAdapterRecyclerView<Int, ItemBackgroundImageAppBinding>() {
    override fun inflateBinding(inflater: LayoutInflater, parent: ViewGroup): ItemBackgroundImageAppBinding {
        return ItemBackgroundImageAppBinding.inflate(inflater, parent, false)
    }

    override fun bindData(binding: ItemBackgroundImageAppBinding, item: Int, position: Int) {
        binding.root.context.loadImage(binding.imgItemBackground, item, R.drawable.background_app_1)
        binding.icPremium.showOrGone(position > 1 && !SharePrefUtils.isBought())
    }
}