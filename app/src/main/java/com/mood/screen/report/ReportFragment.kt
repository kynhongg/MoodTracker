package com.mood.screen.report

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import com.mood.R
import com.mood.base.BaseFragment
import com.mood.data.database.BeanViewModel
import com.mood.data.entity.BeanDailyEntity
import com.mood.data.entity.BeanDefaultEmoji
import com.mood.data.entity.BeanIconEntity
import com.mood.data.entity.BlockEmojiEntity
import com.mood.data.entity.IconEntity
import com.mood.data.entity.MusicCalmEntity
import com.mood.databinding.FragmentReportBinding
import com.mood.screen.addbean.DialogPickTimeSleep
import com.mood.screen.premium.PremiumActivity
import com.mood.utils.CalendarUtil
import com.mood.utils.Constant
import com.mood.utils.DataUtils
import com.mood.utils.SharePrefUtils
import com.mood.utils.isSdkR
import com.mood.utils.openActivity
import com.mood.utils.setGridManager
import com.mood.utils.setOnSafeClick
import com.mood.utils.showOrGone
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import java.util.*


class ReportFragment : BaseFragment<FragmentReportBinding>() {

    companion object {
        const val TAG = Constant.TAG
        const val X_INTERVAL = 1f
        const val MAX_Y_VALUE = 35f
        const val MIN_Y_VALUE = 0f
        const val Y_INTERVAL = 5f
        const val NUMBER_VALUE_Y = 8
    }

    private val viewModel: BeanViewModel by viewModels {
        Constant.getViewModelFactory(requireActivity().application)
    }
    private val dialogPickTime by lazy {
        DialogPickTimeSleep(requireContext())
    }
    private val reportBeanTypeAdapter by lazy {
        ReportBeanTypeAdapter()
    }
    private val iconRankAdapter by lazy {
        IconRankAdapter()
    }
    private var currentMonth = CalendarUtil.getMonthInt() + 1
    private var currentYear = CalendarUtil.getYearInt()
    private var dayOfMonth = 0

    private var listBean: MutableList<BeanDailyEntity> = mutableListOf()
    private var listIcon: MutableList<IconEntity> = mutableListOf()
    private var listBeanIcon: MutableList<BeanIconEntity> = mutableListOf()
    private var listBlock: MutableList<BlockEmojiEntity> = mutableListOf()
    private var listMusicCalmEntity: MutableList<MusicCalmEntity> = mutableListOf()

    lateinit var langAdapter: ArrayAdapter<CharSequence>

    private var isLoadedBean: Boolean = false
    private var isLoadedIcon: Boolean = false
    private var isLoadedBeanIcon: Boolean = false
    private var isLoadedBlock: Boolean = false
    private var handle: Handler = Handler(Looper.getMainLooper())
    private var currentBlockSelected: Int? = null
    override fun initView() {
        if (SharePrefUtils.isCustomBackgroundImage()) {
            binding.root.setBackgroundResource(SharePrefUtils.getBackgroundImageApp())
        }
        dayOfMonth = CalendarUtil.getNumberDayOfMonth(currentMonth, currentYear)
        val month = Calendar.getInstance().get(Calendar.MONTH)
        val year = Calendar.getInstance().get(Calendar.YEAR)
        binding.tvTitleTime.text = CalendarUtil.getTimeTitleMonthYear(requireContext(), month, year)
        binding.rcvBeanType.setGridManager(requireContext(), 4, reportBeanTypeAdapter)
        binding.rcvIconRanking.setGridManager(requireContext(), 1, adapter = iconRankAdapter)
        setupChart()
    }

    override fun initData() {
        queryDatabase()
    }

    private fun queryDatabase() {
        viewModel.getAllIcon {
            isLoadedIcon = true
            listIcon.clear()
            listIcon.addAll(it)
        }
        viewModel.getAllBeanIcon {
            isLoadedBeanIcon = true
            listBeanIcon.clear()
            listBeanIcon.addAll(it)
        }
        viewModel.getAllBlock { blocks ->
            isLoadedBlock = true
            listBlock.clear()
            listBlock.addAll(blocks)
            val list = mutableListOf<CharSequence>()
            list.add(requireContext().getString(R.string.all))
            list.addAll(listBlock.map { it.blockName ?: "" })
            langAdapter = ArrayAdapter<CharSequence>(requireContext(), R.layout.spinner_text, list)
            langAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown)
            binding.spinnerBlockName.adapter = langAdapter
        }
        viewModel.getAllBean { beans ->
            isLoadedBean = true
            listBean.clear()
            listBean.addAll(beans)
            setDataChart(beans)
            setDataSleepStatistic()
        }
        viewModel.getAllMusicCalm { calms ->
            listMusicCalmEntity.clear()
            listMusicCalmEntity.addAll(calms)
            val totalSecond = listMusicCalmEntity.filter { it.year == currentYear }.sumOf { it.second }
            binding.layoutMusicCalm.tvCalmAll.text = DataUtils.getTimeFromSecond(totalSecond)
            setUpDataCalmMusic()
        }
        val runnable = Runnable {
            if (isLoadedBean && isLoadedBeanIcon && isLoadedIcon && isLoadedBlock) {
                setUpIconRanking()
                Log.d(TAG, "setUpIconRanking: loaded")
                handle.removeCallbacksAndMessages(null)
            } else {
                Log.d(TAG, "setUpIconRanking: loading....")
            }
        }
        handle.postDelayed(runnable, 100)
    }

    private fun setUpDataCalmMusic() {
        val totalSecond = listMusicCalmEntity.filter { it.month == currentMonth && it.year == currentYear }
            .sumOf { it.second }
        binding.layoutMusicCalm.tvCalmMonth.text = DataUtils.getTimeFromSecond(totalSecond)
    }

    @SuppressLint("SetTextI18n")
    private fun setDataSleepStatistic() {
        val beans = listBean.filter { it.month == currentMonth && it.year == currentYear }
        val avgBedTime: Int = viewModel.getAvgBedTime(beans)
        val avgSleepTime: Int = viewModel.getAvgSleepTime(beans)
        val avgWakeTime: Int = if (avgBedTime + avgSleepTime > 24 * 60) {
            avgBedTime + avgSleepTime - 24 * 60
        } else {
            avgBedTime + avgSleepTime
        }
        val isPremium = SharePrefUtils.isBought()
        binding.layoutSleepStatistic.tv24hBedtime.text = if (avgBedTime >= 720) "PM" else "AM"
        binding.layoutSleepStatistic.tv24hWaketime.text = if (avgWakeTime >= 720) "PM" else "AM"
        binding.layoutSleepStatistic.tvAvgBedtime.text = if (isPremium) CalendarUtil.formatTime(avgBedTime) else "---"
        binding.layoutSleepStatistic.tvAvgWaketime.text = if (isPremium) CalendarUtil.formatTime(avgWakeTime) else "---"
        binding.layoutSleepStatistic.tvAvgSleep.text = if (isPremium) CalendarUtil.formatTime(avgSleepTime) else "---"
        binding.layoutSleepStatistic.icLock.showOrGone(!isPremium)
        if (!isPremium) {
            binding.layoutSleepStatistic.root.alpha = 0.5f
            binding.layoutSleepStatistic.tvTitleSleep.text = getString(R.string.title_sleep_statistic) + " (Premium feature)"
        } else {
            binding.layoutSleepStatistic.root.alpha = 1f
            binding.layoutSleepStatistic.tvTitleSleep.text = getString(R.string.title_sleep_statistic)
        }
    }

    private fun setUpIconRanking() {
        val dataList =
            viewModel.getDataListIconRanking(
                listIcon, listBeanIcon,
                listBean, currentBlockSelected, currentMonth, currentYear
            )
        iconRankAdapter.setDataList(dataList)
    }

    private fun setDataChart(beans: List<BeanDailyEntity>) {
        setDataToMoodChart(beans)
        setDataPieChart(beans)
    }

    private fun setDataPieChart(dataBean: List<BeanDailyEntity>) {
        val beans = dataBean.filter { it.month == currentMonth }
        setCountBeanType(beans)
        val numberOfBean = beans.size
        val pieEntry = viewModel.getEntriesPieChart(beans)
        val listColor = viewModel.getColorPieChart(beans)

        val dataSet = PieDataSet(pieEntry, "pie chart")
        dataSet.apply {
            selectionShift = 5f
            sliceSpace = 3f
            colors = listColor
            setDrawValues(false)
        }

        val txt1 = numberOfBean.toString() + "\n"
        val moodChart = requireContext().getString(R.string.mood_chart)
        binding.pieChart.apply {
            animateY(400, Easing.EaseInOutCubic)
            centerText = viewModel.getCenterTextPieChartFormat(txt1, moodChart)
            data = PieData(dataSet)
            invalidate()
        }
    }

    private fun setCountBeanType(beans: List<BeanDailyEntity>) {
        val list = mutableListOf<NumberBeanEntity>()
        for (i in 1..8) {
            list.add(NumberBeanEntity(beans.count { it.beanTypeId == i }, BeanDefaultEmoji.values()[i]))
        }
        reportBeanTypeAdapter.setDataList(list)
    }

    override fun initListener() {
        binding.layoutChooseTime.setOnSafeClick {
            showDialogChooseTime()
        }
        binding.spinnerBlockName.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentBlockSelected = when (position) {
                    0 -> null
                    else -> listBlock[position - 1].blockId
                }
                setUpIconRanking()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }
        binding.layoutSleepStatistic.root.setOnSafeClick {
            if (!SharePrefUtils.isBought()) {
                openActivity(PremiumActivity::class.java)
            }
        }
    }

    private fun showDialogChooseTime() {
        if (!dialogPickTime.isShowing()) {
            dialogPickTime.showPickMonthYear(
                getString(R.string.pick_to_show_report),
                true, currentMonth, currentYear
            ) { month, year ->
                currentMonth = month
                currentYear = year
                binding.tvTitleTime.text = CalendarUtil.getTimeTitleMonthYear(requireContext(), month - 1, currentYear)
                dayOfMonth = CalendarUtil.getNumberDayOfMonth(currentMonth, currentYear)
                binding.layoutChart.lineChart.setMaxDayInMoodChart(dayOfMonth)
                setDataChart(listBean)
                setUpIconRanking()
                setDataSleepStatistic()
                setUpDataCalmMusic()
            }
        }
    }

    private fun setupChart() {
        //line chart
        binding.layoutChart.lineChart.apply {
            description.isEnabled = false
            setExtraOffsets(0f, 0f, 0f, 12f)
            setMaxDayInMoodChart(dayOfMonth)
            xAxis.apply {
                setDrawGridLines(false)
                position = XAxis.XAxisPosition.BOTTOM
                axisMaximum = dayOfMonth.toFloat()
                axisMinimum = 1f
                granularity = X_INTERVAL
                textSize = 12f
            }
            axisRight.apply {
                isEnabled = false
            }
            axisLeft.apply {
                setDrawLabels(false)
                setDrawAxisLine(false)
                setDrawGridLines(true)
                enableGridDashedLine(15f, 5f, 0f)
                axisMinimum = MIN_Y_VALUE
                axisMaximum = MAX_Y_VALUE
                granularity = Y_INTERVAL
                labelCount = NUMBER_VALUE_Y
            }
            legend.apply {
                isEnabled = false
            }
            setNoDataText(requireContext().getString(R.string.no_record_bean))
            setupHorizontalScroll()
        }
        binding.pieChart.apply {
            setDrawEntryLabels(false)
            description.isEnabled = false
            legend.isEnabled = false
            isDrawHoleEnabled = true
            holeRadius = 60f
            maxAngle = 180f
            rotationAngle = 180f
            this.isRotationEnabled = false
            setCenterTextSize(20f)
            setCenterTextColor(ContextCompat.getColor(requireContext(), R.color.text_color))
            setCenterTextTypeface(ResourcesCompat.getFont(requireContext(), R.font.nunito_bold_700))
            setCenterTextOffset(0f, -30f)
            setExtraOffsets(0f, 0f, 0f, -100f)
            setNoDataText(requireContext().getString(R.string.no_record_bean))
        }
    }

    private fun LineChart.setMaxDayInMoodChart(maxValue: Int) {
        this.xAxis.labelCount = maxValue
        //update max value of X-Axis
        this.xAxis.axisMaximum = maxValue.toFloat()
    }

    private fun setupHorizontalScroll() {
        val displayMetrics = DisplayMetrics()
        if (isSdkR()) {
            @Suppress("DEPRECATION")
            requireContext().display?.getRealMetrics(displayMetrics)
        } else {
            @Suppress("DEPRECATION")
            requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        }
        val width = displayMetrics.widthPixels
        val defaultWidth = 20
        val defaultPie = 8
        if (width < defaultWidth * width / defaultPie) {
            binding.layoutChart.lineChart.layoutParams =
                LinearLayout.LayoutParams(defaultWidth * width / defaultPie, binding.layoutChart.lineChart.layoutParams.height)
        } else {
            binding.layoutChart.lineChart.layoutParams =
                LinearLayout.LayoutParams(width, binding.layoutChart.lineChart.layoutParams.height)
        }
    }

    private fun setDataToMoodChart(beans: List<BeanDailyEntity>) {
        val dataList = beans.filter {
            it.month == currentMonth
        }.map {
            DataUtils.convertBeanToChartEntry(it)
        }.sortedBy { it.x }
        val weekOneSales = LineDataSet(dataList, "")
        weekOneSales.apply {
            setDrawValues(false)
            setDrawCircles(true)
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.text_color))
            circleRadius = 2f
            setDrawCircleHole(false)
            lineWidth = 1.5f
            mode = LineDataSet.Mode.LINEAR
            color = ContextCompat.getColor(requireContext(), R.color.salmon)
        }
        val dataSet = ArrayList<ILineDataSet>()
        dataSet.add(weekOneSales)
        binding.layoutChart.lineChart.apply {
            animateX(400, Easing.EaseInSine)
            data = LineData(dataSet)
            data.isHighlightEnabled = false
            invalidate()
        }
    }

    override fun inflateLayout(inflater: LayoutInflater, container: ViewGroup?): FragmentReportBinding {
        return FragmentReportBinding.inflate(inflater, container, false)
    }
}