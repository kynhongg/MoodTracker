package com.mood.screen.setting.app

import android.view.LayoutInflater
import com.mood.R
import com.mood.base.BaseActivity
import com.mood.databinding.ActivityChooseBackgroundImageBinding
import com.mood.screen.premium.PremiumActivity
import com.mood.utils.SharePrefUtils
import com.mood.utils.gone
import com.mood.utils.hide
import com.mood.utils.openActivity
import com.mood.utils.setGridManager
import com.mood.utils.setOnSafeClick
import com.mood.utils.show

class ChooseBackgroundImageActivity : BaseActivity<ActivityChooseBackgroundImageBinding>() {
    private var isPreview = false

    private val imageBackgroundAdapter by lazy {
        ImageBackgroundAdapter()
    }

    override fun initView() {
        binding.rcvImage.setGridManager(this, 2, imageBackgroundAdapter)
        binding.btnDone.tvButtonDone.text = getString(R.string.txt_use_theme)
        if (SharePrefUtils.isCustomBackgroundImage()) {
            binding.root.setBackgroundResource(SharePrefUtils.getBackgroundImageApp())
        }
    }

    override fun initData() {
        imageBackgroundAdapter.setDataList(
            mutableListOf(
                R.drawable.background_app_1,
                R.drawable.background_app_2,
                R.drawable.background_app_3,
                R.drawable.background_app_4,
                R.drawable.background_app_5
            )
        )
    }

    private var imageSelect = 0
    override fun initListener() {
        binding.btnBack.setOnSafeClick {
            onBack()
        }
        binding.layoutBtnBack.setOnSafeClick {
            onBack()
        }

        imageBackgroundAdapter.setOnClickItem { item, position ->
            if (position > 1 && !SharePrefUtils.isBought()) {
                openActivity(PremiumActivity::class.java)
//                showToast(getString(R.string.unlock_with_premium_version))
            } else {
                isPreview = true
                item?.let {
                    binding.root.setBackgroundResource(it)
                    imageSelect = it
                }
                binding.rcvImage.hide()
                binding.layoutButtonDone.show()
            }
        }
        binding.layoutButtonDone.setOnSafeClick {
            if (imageSelect == R.drawable.background_app_1) {
                SharePrefUtils.setIsCustomBackgroundImage(false)
                SharePrefUtils.setBackgroundImageApp(R.drawable.background_app_1)
            } else {
                SharePrefUtils.setIsCustomBackgroundImage(true)
                SharePrefUtils.setBackgroundImageApp(imageSelect)
            }
            isPreview = false
            onBack()
        }
    }

    override fun inflateViewBinding(inflater: LayoutInflater): ActivityChooseBackgroundImageBinding {
        return ActivityChooseBackgroundImageBinding.inflate(inflater)
    }

    private fun onBack() {
        if (isPreview) {
            isPreview = false
            binding.rcvImage.show()
            binding.layoutButtonDone.gone()
            binding.root.setBackgroundResource(SharePrefUtils.getBackgroundImageApp())
        } else {
            finish()
        }
    }

    override fun onBackPressed() {
        onBack()
    }
}