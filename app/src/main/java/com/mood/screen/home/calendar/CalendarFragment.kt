package com.mood.screen.home.calendar

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.mood.R
import com.mood.base.BaseFragment
import com.mood.base.NeedPermissionDialog
import com.mood.data.database.BeanViewModel
import com.mood.data.entity.BeanDailyEntity
import com.mood.data.entity.BeanDefaultEmoji
import com.mood.data.entity.BeanIconEntity
import com.mood.data.entity.BeanImageAttachEntity
import com.mood.data.entity.IconEntity
import com.mood.databinding.FragmentCalendarBinding
import com.mood.screen.addbean.AddBeanActivity
import com.mood.screen.home.share.ShareImageActivity
import com.mood.screen.setting.content.dialog.DialogConfirmRemoveIcon
import com.mood.screen.timeline.TimeLineActivity
import com.mood.utils.CalendarUtil
import com.mood.utils.Constant
import com.mood.utils.Constant.WeekString
import com.mood.utils.SharePrefUtils
import com.mood.utils.getActivityResultLauncher
import com.mood.utils.gone
import com.mood.utils.hasReadStoragePermission
import com.mood.utils.loadImage
import com.mood.utils.openActivity
import com.mood.utils.requestPermissionReadStorage
import com.mood.utils.setGridManager
import com.mood.utils.show
import com.mood.utils.showOrGone
import com.mood.utils.showToast
import com.mood.utils.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.Calendar
import java.util.Collections

const val TAG = Constant.TAG

//Not use, using CalendarFragmentV2
class CalendarFragment : BaseFragment<FragmentCalendarBinding>() {

    //region variable
    private var weekTitle = mutableListOf<String>()

    private var listIcon: MutableList<IconEntity> = mutableListOf()
    private var listBean: MutableList<BeanDailyEntity> = mutableListOf()
    private var listBeanIcon: MutableList<BeanIconEntity> = mutableListOf()
    private var listBeanImageAttach: MutableList<BeanImageAttachEntity> = mutableListOf()

    private var dataList: MutableList<CalendarEntity> = mutableListOf()
    private var beanSelect: BeanDailyEntity? = null
    private var isLoadFirst = false
    private var iconFilter = 0
    private var sourceId: Int = 0
    private val isSetUpData by lazy {
        arguments?.getBoolean(Constant.IS_SET_UP_DATE, true) ?: kotlin.run {
            true
        }
    }
    private val viewModel: BeanViewModel by viewModels {
        Constant.getViewModelFactory(requireActivity().application)
    }
    private val calendarAdapter by lazy {
        CalendarAdapter()
    }
    private val beanIconAdapter by lazy {
        BeanIconAdapter()
    }
    private val dialogConfirmRemoveRecord by lazy {
        DialogConfirmRemoveIcon(requireContext())
    }

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var isReadPermissionGranted = false

    companion object {
        const val TO_RIGHT_ONE = -1
        const val TO_RIGHT_TWO = -2
        const val TO_RIGHT_THREE = -3
        const val TO_RIGHT_FOUR = -4
        const val TO_RIGHT_FIVE = -5
        const val TO_RIGHT_SIX = -6
    }

    private var startWith: WeekString = WeekString.Sunday
    private var currentTime: Calendar = Calendar.getInstance()

    //endregion
    fun updateUI(monthYear: Calendar) {
        currentTime = monthYear
        setupDay()
    }

    private fun getSpaceFirstCalendar(firstDayOfMonth: Int): Int {
        if (firstDayOfMonth == startWith.number) {// start day is first day of the month
            return 0
        }
        val listDay = mutableListOf(
            requireContext().getString(R.string.sunday),
            requireContext().getString(R.string.monday),
            requireContext().getString(R.string.tuesday),
            requireContext().getString(R.string.wednesday),
            requireContext().getString(R.string.thursday),
            requireContext().getString(R.string.friday),
            requireContext().getString(R.string.saturday)
        )
        return weekTitle.indexOf(listDay[firstDayOfMonth - 1])
    }

    override fun initView() {
        binding.rcvCalendar.setGridManager(requireContext(), 7, calendarAdapter)
        binding.rcvIconBean.setGridManager(requireContext(), 7, beanIconAdapter)
        if (SharePrefUtils.isCustomBackgroundImage()) {
            binding.root.setBackgroundResource(SharePrefUtils.getBackgroundImageApp())
        }
    }

    override fun initData() {
        isReadPermissionGranted = hasReadStoragePermission()
        permissionLauncher = getActivityResultLauncher { permissions ->
            isReadPermissionGranted = hasReadStoragePermission()
            if (isReadPermissionGranted) {
                showToast("Reload to update data")
            }
        }
        iconFilter = Constant.iconFilter
        sourceId = Constant.sourceId
        initCalendar()
        viewModel.allIcons.observe(this) {
            listIcon.clear()
            listIcon.addAll(it)
        }
        viewModel.allBeanIcon.observe(this) {
            listBeanIcon.clear()
            listBeanIcon.addAll(it)
            if (Constant.isFilter) {
                filterByIconId(iconFilter, sourceId)
            }
        }
        viewModel.allBeanImageAttach.observe(this) {
            listBeanImageAttach.clear()
            listBeanImageAttach.addAll(it)
        }
        viewModel.allBeans.observe(this) {
            listBean.clear()
            listBean.addAll(it)
            Log.d(TAG, "CalendarFragment - initData: beans: ${listBean.size}")
            setupDay()
        }
    }

    private fun initCalendar() {
        setUpWeekTitle(startWith)
//        addDay()
    }

    private fun setupDay() {
        Log.d(TAG, "addDay: ")
        isLoadFirst = false
        dataList.clear()
        val calendar = currentTime
        calendar[Calendar.DAY_OF_MONTH] = 1
        val firstDayOfMonth = calendar[Calendar.DAY_OF_WEEK]
        val numberOfSpace = getSpaceFirstCalendar(firstDayOfMonth)
        repeat(numberOfSpace) {
            dataList.add(CalendarEntity(value = "", isShow = false, day = 0, month = 0, year = 0))
        }
        val month = currentTime.get(Calendar.MONTH)
        val year = currentTime.get(Calendar.YEAR)
        loadCalendarData(listBean, month + 1, year)
        calendarAdapter.setDataCalendar(dataList) {
            val itemSelected = calendarAdapter.getSelectedItem()
            itemSelected?.let { item ->
                val bean = listBean.find {
                    it.day == item.day
                            && it.month == item.month
                            && it.year == item.year
                }
                if (bean != null) {
                    setUpViewWithBean(bean)
                } else {
                    calendarAdapter.clearSelected()
                }
            }
        }
    }

    private fun loadCalendarData(allBean: List<BeanDailyEntity>, month: Int, year: Int) {
        val dayOfMonth = when {
            month == 2 && year % 4 == 0 && year % 100 != 0 || year % 400 == 0 -> 29
            month == 2 -> 28
            else -> CalendarUtil.getDayCountOfMonth(month) ?: 30
        }
        val beanInMonth = allBean.filter { it.month == month && it.year == year }
        val list = List(dayOfMonth) { item ->
            val currentDay = item + 1
            val icon = if (!Constant.isFilter) {
                getBeanIcon(beanInMonth, currentDay)
            } else {
                getIconFilter(beanInMonth, currentDay)
            }
            CalendarEntity(
                day = currentDay,
                month = month,
                year = year,
                value = currentDay.toString(),
                beanIcon = icon,
                isToday = CalendarUtil.checkToday(currentDay, month, year),
                isFeature = CalendarUtil.checkFeatures(currentDay, month, year),
            )
        }
        dataList.addAll(list)
    }

    private fun getIconFilter(allBean: List<BeanDailyEntity>, currentDay: Int): Int {
        val bean = allBean.find {
            it.day == currentDay
        }
        return if (bean == null) {
            R.drawable.ic_bean_type_default
        } else {
            val beanIconId = bean.beanIconId
            if (bean.beanTypeId == -iconFilter) {
                return BeanDefaultEmoji.getImageIdByIndex(-iconFilter)
            } else {
                val list = listBeanIcon.filter { it.beanIconId == beanIconId }
                if (iconFilter in list.map { it.iconId }) {
                    return sourceId
                } else {
                    R.drawable.ic_bean_type_default
                }
            }
        }
    }

    private fun getBeanIcon(allBean: List<BeanDailyEntity>, day: Int): Int {
        val iconId = allBean.singleOrNull {
            it.day == day
        }?.beanTypeId
        return if (iconId == null) {
            R.drawable.ic_bean_type_default
        } else {
            return BeanDefaultEmoji.getImageIdByIndex(iconId)
        }
    }

    override fun initListener() {
        var listBeanInDay: MutableList<BeanDailyEntity>
        calendarAdapter.setOnClickItem { item, position ->
            calendarAdapter.setSelectedItem(position)
            item?.let {
                if (item.isFeature) {
                    showToast(requireContext().getString(R.string.cannot_choose_future))
                }
                listBeanInDay = listBean.filter { bean ->
                    bean.year == item.year && bean.month == item.month && bean.day == item.day
                }.toMutableList()
//                if (listBeanInDay.isNotEmpty() && it.beanIcon != R.drawable.ic_bean_type_default) {
                if (listBeanInDay.isNotEmpty()) {
                    setUpViewWithBean(listBeanInDay.first())
                } else {
                    hideBlockBean()
                    if (!item.isFeature) {
                        openActivity(
                            AddBeanActivity::class.java, bundle = bundleOf(
                                Constant.TYPE to 1,
                                Constant.IS_ADD to true,
                                Constant.BEAN_DAY to item.day,
                                Constant.BEAN_MONTH to item.month,
                                Constant.BEAN_YEAR to item.year
                            )
                        )
                    }
                }
            }
        }
        //click layout bean
        binding.imgShareBean.setOnClickListener {
            gotoShareScreen()
        }
        binding.imgShareBean2.setOnClickListener {
            gotoShareScreen()
        }

        binding.imgEditBean.setOnClickListener {
            goToEditBean()
        }
        binding.imgEditBean2.setOnClickListener {
            goToEditBean()
        }

        binding.imgRemoveBean.setOnClickListener {
            showDialogRemoveBean()
        }
        binding.imgRemoveBean2.setOnClickListener {
            showDialogRemoveBean()
        }
        binding.btnSeeAllTimeLine.setOnClickListener {
            openActivity(TimeLineActivity::class.java)
        }
    }

    private fun gotoShareScreen() {
        exportBeanRecord { calendarBitmap ->
            val stream = ByteArrayOutputStream()
            calendarBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val byteArray: ByteArray = stream.toByteArray()
            openActivity(
                ShareImageActivity::class.java,
                bundle = Bundle().apply {
                    putByteArray(Constant.IMAGE_SHARE, byteArray)
                }
            )
        }
    }

    private fun showDialogRemoveBean() {
        if (!dialogConfirmRemoveRecord.isShowing()) {
            dialogConfirmRemoveRecord.show(requireContext().getString(R.string.do_you_want_remove_this_record)) {
                beanSelect?.let { bean ->
                    Log.d(TAG, "initListener: remove $bean")
                    lifecycleScope.launch(Dispatchers.IO) {
                        listBean.remove(bean)
                        viewModel.deleteBeanById(bean.beanId)
                        bean.beanIconId?.let { it -> viewModel.deleteBeanIconByBeanIconId(it) }
                    }
                }
            }
        }
    }

    private fun goToEditBean() {
        openActivity(
            AddBeanActivity::class.java, bundle = bundleOf(
                Constant.TYPE to beanSelect?.beanTypeId,
                Constant.IS_ADD to false,
                Constant.BEAN_EDIT to beanSelect,
                Constant.BEAN_DAY to beanSelect?.day,
                Constant.BEAN_MONTH to beanSelect?.month,
                Constant.BEAN_YEAR to beanSelect?.year
            )
        )
    }

    private fun hideBlockBean() {
        binding.layoutFirstBean.gone()
        binding.btnSeeAllTimeLine.gone()
    }

    private fun setUpViewWithBean(bean: BeanDailyEntity) {
        beanSelect = bean
        val index = bean.beanTypeId ?: 0
        val list = listBeanIcon.filter { it.beanIconId == bean.beanIconId }.map { it.iconId }
        beanIconAdapter.setDataList(listIcon.filter { it.iconId in list })
        //attach image
        val listImageAttach = listBeanImageAttach.filter {
            it.beanId == bean.beanId
        }
        val isShow =
            !(listImageAttach.isEmpty() && list.isEmpty() && bean.beanDescription.isNullOrEmpty())
        val txtStatus = BeanDefaultEmoji.getStatusByIndex(requireContext(), index)
        fillContent(index, bean, txtStatus, isShow, list)
        fillImage(listImageAttach)
    }

    private fun fillContent(
        index: Int,
        bean: BeanDailyEntity,
        txtStatus: String,
        isShow: Boolean,
        list: List<Int?>
    ) {
        binding.btnSeeAllTimeLine.show()
        binding.imgBean.setImageResource(BeanDefaultEmoji.getImageIdByIndex(index))
        binding.layoutFirstBean.apply {
            show()
            setBackgroundResource(BeanDefaultEmoji.getBackgroundResourceByIndex(index))
        }
        binding.tvTimeCreateBean.text = CalendarUtil.formatTime(bean.hour, bean.minutes)
        binding.tvBeanStatus.apply {
            text = txtStatus
            showOrGone(isShow)
        }
        binding.tvBeanStatus2.apply {
            text = txtStatus
            showOrGone(!isShow)
        }
        binding.tvBeanContent.apply {
            text = bean.beanDescription
            showOrGone(bean.beanDescription?.isNotEmpty() == true)
        }
        binding.rcvIconBean.showOrGone(list.isNotEmpty())
        binding.lineHorizontal.showOrGone(isShow)
        binding.imgRemoveBean.showOrGone(isShow)
        binding.imgRemoveBean2.showOrGone(!isShow)
        binding.imgEditBean.showOrGone(isShow)
        binding.imgEditBean2.showOrGone(!isShow)
        binding.imgShareBean.showOrGone(isShow)
        binding.imgShareBean2.showOrGone(!isShow)
    }

    private fun fillImage(listImageAttach: List<BeanImageAttachEntity>) {
        val imageViews = arrayOf(binding.imgChoose1, binding.imgChoose2, binding.imgChoose3)
        val cardImageViews = arrayOf(binding.cardImg1, binding.cardImg2, binding.cardImg3)

        for (i in imageViews.indices) {
            if (i < listImageAttach.size) {
                cardImageViews[i].show()
                val url = listImageAttach[i].urlImage ?: ""
                isReadPermissionGranted = hasReadStoragePermission()
                if (!isReadPermissionGranted) {
                    if (Constant.isShowDialogNeedPermission) {
                        showDialogNeedPermission()
                    }
                }
                requireContext().loadImage(imageViews[i], url)
            } else {
                cardImageViews[i].gone()
            }
        }
    }

    private fun showDialogNeedPermission() {
        NeedPermissionDialog(requireContext()).also { dialog ->
            dialog.show(onClickDone = {
                Constant.isShowDialogNeedPermission = false
                requestPermissionReadStorage(permissionLauncher)
            }, onCLickCLose = {
                Constant.isShowDialogNeedPermission = false
            })
        }
    }

    private fun setUpWeekTitle(weekString: WeekString) {
        //start with Monday
        weekTitle = Constant.getWeekTitleStartWithMonday(requireContext())
        //rotate week title
        when (weekString) {
            WeekString.Tuesday -> Collections.rotate(weekTitle, TO_RIGHT_ONE)
            WeekString.Wednesday -> Collections.rotate(weekTitle, TO_RIGHT_TWO)
            WeekString.Thursday -> Collections.rotate(weekTitle, TO_RIGHT_THREE)
            WeekString.Friday -> Collections.rotate(weekTitle, TO_RIGHT_FOUR)
            WeekString.Saturday -> Collections.rotate(weekTitle, TO_RIGHT_FIVE)
            WeekString.Sunday -> Collections.rotate(weekTitle, TO_RIGHT_SIX)
            else -> {}
        }
    }

    fun exportCalendar(success: (Bitmap) -> Unit) {
        if (isAdded) {
            binding.rcvCalendar.toBitmap().let {
                success.invoke(it)
            }
        } else {
            success.invoke(Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888))
        }
    }

    private fun exportBeanRecord(success: (Bitmap) -> Unit) {
        if (isAdded) {
            binding.layoutFirstBean.toBitmap().let {
                success.invoke(it)
            }
        } else {
            success.invoke(Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888))
        }
    }

    private fun filterByIconId(iconId: Int, sourceId: Int) {
        iconFilter = iconId
        this.sourceId = sourceId
        setupDay()
    }

    override fun inflateLayout(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCalendarBinding {
        return FragmentCalendarBinding.inflate(inflater, container, false)
    }

}