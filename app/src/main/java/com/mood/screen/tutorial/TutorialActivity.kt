package com.mood.screen.tutorial

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import androidx.viewpager2.widget.ViewPager2
import com.mood.R
import com.mood.base.BaseActivity
import com.mood.databinding.ActivityTutorialBinding
import com.mood.screen.home.MainActivity
import com.mood.utils.Constant
import com.mood.utils.SharePrefUtils
import com.mood.utils.openActivity
import com.mood.utils.requestNotifyPermission
import com.mood.utils.showToast

class TutorialActivity : BaseActivity<ActivityTutorialBinding>() {
    private val viewPagerAdapter by lazy {
        ViewPagerTutorialAdapter(supportFragmentManager, lifecycle)
    }
    private val callbackViewPager = object : ViewPager2.OnPageChangeCallback() {
        @SuppressLint("SetTextI18n")
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            when (TutorialStep.getTabByPosition(position)) {
                TutorialStep.One -> {
                    binding.imgStep.setImageResource(R.drawable.img_step_1)
                    binding.tvStep.text = "Record Your Feelings Every Day"
                    binding.tvBtnNext.text = getString(R.string.text_continue)
                }

                TutorialStep.Two -> {
                    binding.imgStep.setImageResource(R.drawable.img_step_2)
                    binding.tvStep.text = "Track Emotions With Charts"
                    binding.tvBtnNext.text = getString(R.string.text_continue)
                }

                TutorialStep.Three -> {
                    binding.imgStep.setImageResource(R.drawable.img_step_3)
                    binding.tvStep.text = "Smoothe Emotions With Music"
                    binding.tvBtnNext.text = getString(R.string.start)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.viewPager.registerOnPageChangeCallback(callbackViewPager)
    }

    override fun onPause() {
        super.onPause()
        binding.viewPager.unregisterOnPageChangeCallback(callbackViewPager)
    }

    override fun initView() {
        if (SharePrefUtils.isFirstRequestNotification()) {
            requestNotifyPermission()
            SharePrefUtils.setFirstRequestNotification(false)
        }
    }


    override fun initData() {
        binding.viewPager.adapter = viewPagerAdapter
        binding.viewPager.setCurrentItem(0, true)
    }

    override fun initListener() {
        binding.btnNext.setOnClickListener {
            if (isLastPage()) {
                startApp()
            } else {
                val position = binding.viewPager.currentItem
                binding.viewPager.currentItem = position + 1
            }
        }
    }

    fun isLastPage() =
        TutorialStep.getTabByPosition(binding.viewPager.currentItem) == TutorialStep.Three

    private fun startApp() {
        SharePrefUtils.saveKey(Constant.IS_FIRST_OPEN, true)
        openActivity(MainActivity::class.java, isFinish = true)
//        if (SharePrefUtils.isBought()) {
//        } else {
//            openActivity(
//                PremiumActivity::class.java, isFinish = true, bundleOf(
//                    Constant.IS_FROM_START_APP to true
//                )
//            )
//        }
    }

    override fun inflateViewBinding(inflater: LayoutInflater): ActivityTutorialBinding {
        return ActivityTutorialBinding.inflate(inflater)
    }

    private var isClickBack = false

    private fun onBack() {
        if (isClickBack) {
            finish()
        } else {
            showToast(getString(R.string.click_back))
            isClickBack = true
            Handler(Looper.getMainLooper()).postDelayed({
                isClickBack = false
            }, 1000L)
        }
    }

    override fun onBackPressed() {

    }
}