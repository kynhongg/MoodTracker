package com.mood.screen.home

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import com.mood.R
import com.mood.base.BaseActivity
import com.mood.data.database.BeanViewModel
import com.mood.data.entity.BeanDailyEntity
import com.mood.data.entity.BeanDefaultEmoji
import com.mood.databinding.ActivityMainBinding
import com.mood.screen.addbean.AddBeanActivity
import com.mood.utils.CalendarUtil
import com.mood.utils.Constant
import com.mood.utils.SharePrefUtils
import com.mood.utils.openActivity
import com.mood.utils.setFullScreenMode
import com.mood.utils.setGridManager
import com.mood.utils.setOnSafeClick
import com.mood.utils.showDialogRating
import com.mood.utils.showOrGone
import com.mood.utils.showToast
import java.util.Calendar

class MainActivity : BaseActivity<ActivityMainBinding>() {
    private var tabSelected = TabSelect.TabCalendar
    private var isShowDialog = false
    private val listBeanRecord = mutableListOf<BeanDailyEntity>()
    private val viewModel: BeanViewModel by viewModels {
        Constant.getViewModelFactory(application)
    }
    private val chooseBeanTypeAdapter by lazy {
        ChooseBeanTypeAdapter()
    }

    override fun initView() {
        setFullScreenMode(SharePrefUtils.isFullScreenMode())
        binding.rcvBeanType.setGridManager(this, 4, chooseBeanTypeAdapter)
        binding.btnCalendar.isEnabled = tabSelected != TabSelect.TabCalendar
        binding.btnRelax.isEnabled = tabSelected != TabSelect.TabRelax
        binding.btnReport.isEnabled = tabSelected != TabSelect.TabReport
        binding.btnSetting.isEnabled = tabSelected != TabSelect.TabSetting
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isShowDialog) {
                    isShowDialog = false
                    setUpDialogChooseBean()
                } else {
                    if (supportFragmentManager.backStackEntryCount == 0) {
                        onBack()
                    }
                }
            }
        })
    }

    override fun initData() {
        val count = SharePrefUtils.getInt(Constant.COUNT_OPEN_APP)
        SharePrefUtils.saveKey(Constant.COUNT_OPEN_APP, count + 1)
        viewModel.allBeans.observe(this) {
            listBeanRecord.clear()
            listBeanRecord.addAll(it)
        }
        chooseBeanTypeAdapter.setDataList(
            BeanDefaultEmoji.values().toMutableList().subList(1, BeanDefaultEmoji.values().size)
        )
        Constant.isCountClickSave.observe(this) {
            Log.d(Constant.TAG, "isCountClickSave: $it")
            if (it == 1
                && SharePrefUtils.getInt(Constant.COUNT_OPEN_APP) == 2
                && SharePrefUtils.getBoolean(Constant.IS_SHOW_DIALOG, defaultValue = true)
                && Calendar.getInstance().timeInMillis > SharePrefUtils.getLong(Constant.TIME_TO_REPEAT_RATE)
                && Constant.isShowRateApp
            ) {
                Handler(Looper.getMainLooper()).postDelayed({
                    showDialogRating()
                }, 500)
            }
        }
    }

    override fun initListener() {
        binding.btnEmoji.setOnSafeClick {
            isShowDialog = !isShowDialog
            setUpDialogChooseBean()
        }
        binding.btnCalendar.setOnSafeClick {
            if (!isShowDialog) {
                setupViewBottom(TabSelect.TabCalendar)
            }
        }
        binding.btnRelax.setOnSafeClick {
            if (!isShowDialog) {
                setupViewBottom(TabSelect.TabRelax)
            }
        }
        binding.btnReport.setOnSafeClick {
            if (!isShowDialog) {
                setupViewBottom(TabSelect.TabReport)
            }
        }
        binding.btnSetting.setOnSafeClick {
            if (!isShowDialog) {
                setupViewBottom(TabSelect.TabSetting)
            }
        }
        val currentDay = CalendarUtil.getDayInt()
        val currentMonth = CalendarUtil.getMonthInt() + 1
        val currentYear = CalendarUtil.getYearInt()
        chooseBeanTypeAdapter.setOnClickItem { item, position ->
            Log.d(Constant.TAG, "initListener: $item")
            closeDialogChooseBean()
            //navigate to screen add bean
            val index = position + 1
            val list = listBeanRecord.filter { it.day == currentDay && it.month == currentMonth && it.year == currentYear }
            val isAdd = list.isEmpty()
            openActivity(
                AddBeanActivity::class.java, bundleOf(
                    Constant.TYPE to index,
                    Constant.IS_ADD to isAdd,
                    Constant.BEAN_EDIT to list.firstOrNull(),
                    Constant.BEAN_DAY to CalendarUtil.getDayInt(),
                    Constant.BEAN_MONTH to CalendarUtil.getMonthInt() + 1,
                    Constant.BEAN_YEAR to CalendarUtil.getYearInt()
                )
            )
        }
        binding.btnCloseAddBean.setOnSafeClick {
            closeDialogChooseBean()
        }
        binding.layoutDialogChooseBeanType.setOnSafeClick {
            closeDialogChooseBean()
        }
    }

    private fun closeDialogChooseBean() {
        if (isShowDialog) {
            isShowDialog = false
            setUpDialogChooseBean()
        }
    }

    private fun setUpDialogChooseBean() {
        binding.layoutDialogChooseBeanType.showOrGone(isShowDialog)
    }

    private fun setupViewBottom(tabSelect: TabSelect) {
        tabSelected = tabSelect
        binding.btnCalendar.isEnabled = tabSelect != TabSelect.TabCalendar
        binding.btnRelax.isEnabled = tabSelect != TabSelect.TabRelax
        binding.btnReport.isEnabled = tabSelect != TabSelect.TabReport
        binding.btnSetting.isEnabled = tabSelect != TabSelect.TabSetting
        val resource = when (tabSelect) {
            TabSelect.TabCalendar -> {
                R.drawable.ic_calendar_selected
            }

            TabSelect.TabRelax -> {
                R.drawable.ic_relax_selected
            }

            TabSelect.TabReport -> {
                R.drawable.ic_report_selected
            }

            TabSelect.TabSetting -> {
                R.drawable.ic_setting_selected
            }
        }
        binding.btnCalendar.setImageResource(if (tabSelect == TabSelect.TabCalendar) resource else R.drawable.ic_calendar)
        binding.btnRelax.setImageResource(if (tabSelect == TabSelect.TabRelax) resource else R.drawable.ic_relax)
        binding.btnReport.setImageResource(if (tabSelect == TabSelect.TabReport) resource else R.drawable.ic_report)
        binding.btnSetting.setImageResource(if (tabSelect == TabSelect.TabSetting) resource else R.drawable.ic_setting)
        navigateToScreen(tabSelect)
    }

    private fun navigateToScreen(tabSelect: TabSelect) {
        val navController = binding.navHostFragment.findNavController()
        val screenId = when (tabSelect) {
            TabSelect.TabCalendar -> R.id.homeFragment
            TabSelect.TabRelax -> R.id.relaxFragment
            TabSelect.TabReport -> R.id.reportFragment
            TabSelect.TabSetting -> R.id.settingFragment
        }
        navController.navigate(screenId)
    }

    fun setupLayer(isShow: Boolean) {
        binding.layerAlpha.showOrGone(isShow)
    }

    override fun inflateViewBinding(inflater: LayoutInflater): ActivityMainBinding {
        return ActivityMainBinding.inflate(inflater)
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
}