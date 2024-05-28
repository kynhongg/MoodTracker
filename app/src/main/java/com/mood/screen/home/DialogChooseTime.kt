package com.mood.screen.home

import android.content.Context
import android.view.LayoutInflater
import android.view.Window
import androidx.appcompat.app.AlertDialog
import com.mood.R
import com.mood.databinding.LayoutDialogChooseTimeBinding
import com.mood.utils.CalendarUtil
import com.mood.utils.gone
import com.mood.utils.setGridManager
import com.mood.utils.setOnSafeClick
import com.mood.utils.show
import com.mood.utils.showToast

class DialogChooseTime(val context: Context) {
    companion object {
        const val COUNT_YEAR_BACK = 9
        const val COUNT_YEAR_NEXT = 4
    }

    private var isSelectMonth = true
    private val binding by lazy {
        LayoutDialogChooseTimeBinding.inflate(LayoutInflater.from(context))
    }
    private val dialog: AlertDialog by lazy {
        AlertDialog.Builder(context, R.style.dialog_transparent_width).setView(binding.root)
            .create()
    }

    private val chooseTimeAdapter by lazy {
        ChooseTimeAdapter()
    }

    init {
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
    }

    fun isShowing(): Boolean {
        return dialog.isShowing
    }

    fun hide() {
        dialog.dismiss()
    }

    fun show(
        currentYear: String,
        currentMonthSelect: String,
        cancelAble: Boolean? = true,
        onClickSubmit: ((item: ChooseTimeEntity) -> Unit)? = null
    ) {
        binding.btnSelectYear.show()
        binding.tvYear.show()
        binding.rcvTime.setGridManager(context, 4, chooseTimeAdapter)
        binding.tvYear.text = currentYear
        dialog.setCancelable(cancelAble ?: false)
        setUpDataAdapter(currentMonthSelect, currentYear.toInt())
        chooseTimeAdapter.setOnClickItem { item, position ->
            if (isSelectMonth) {
                val year = binding.tvYear.text.toString().toInt()
                if (year > CalendarUtil.getYearInt() || (year == CalendarUtil.getYearInt()
                            && CalendarUtil.isFuture(context, item?.month ?: ""))
                ) {
                    context.also { ct ->
                        ct.showToast(ct.getString(R.string.cannot_choose_future))
                    }
                } else {
                    chooseTimeAdapter.setSelectedItem(position)
                    item?.let { onClickSubmit?.invoke(it) }
                    dialog.dismiss()
                }
            } else {
                isSelectMonth = true
                binding.tvYear.text = item?.year.toString()
                binding.btnSelectYear.show()
                binding.tvYear.show()
                if (binding.tvYear.text.toString().toInt() == currentYear.toInt()) {
                    setUpDataAdapter(currentMonthSelect, binding.tvYear.text.toString().toInt())
                } else {
                    setUpDataAdapter("", binding.tvYear.text.toString().toInt())
                }
            }
        }
        binding.btnSelectYear.setOnSafeClick {
            binding.btnSelectYear.gone()
            binding.tvYear.gone()
            setSelectYear()
        }
        binding.btnBackTime.setOnSafeClick {
            changeData(binding.tvYear.text.toString().toInt() - 1, currentYear, currentMonthSelect)
        }
        binding.btnNextTime.setOnSafeClick {
            val year = binding.tvYear.text.toString().toInt() + 1
            if (year <= CalendarUtil.getYearInt()) {
                changeData(binding.tvYear.text.toString().toInt() + 1, currentYear, currentMonthSelect, true)
            }
        }
        if (!dialog.isShowing)
            dialog.show()
    }

    private fun setUpDataAdapter(currentMonthSelect: String, yearSelect: Int) {
        val listMonth = CalendarUtil.getListMonthString(context)
        val indexOf = listMonth.indexOfFirst { it.contentEquals(currentMonthSelect, true) }
        val data = mutableListOf<ChooseTimeEntity>()
        listMonth.forEachIndexed { index, s ->
            if (index != indexOf) {
                data.add(ChooseTimeEntity(s, yearSelect, true, isSelected = false, monthInt = index + 1))
            } else {
                data.add(ChooseTimeEntity(s, yearSelect, true, isSelected = true, monthInt = index + 1))
            }
        }
        chooseTimeAdapter.setDataList(data)
    }

    private fun changeData(year: Int, currentYear: String, currentMonthSelect: String, isNext: Boolean = false) {
        binding.tvYear.text = year.toString()
        if (isSelectMonth) {
            chooseTimeAdapter.updateYearAllData(year)
            if (year == currentYear.toInt()) {
                chooseTimeAdapter.setSelectedItem(
                    CalendarUtil.getListMonthString(context)
                        .indexOf(currentMonthSelect)
                )
            } else {
                chooseTimeAdapter.resetSelect()
            }
        } else {
            if (!isNext) {
                val mYear = chooseTimeAdapter.dataList.first().year - COUNT_YEAR_BACK
                if (mYear >= CalendarUtil.getYearInt() - 20) {
                    chooseTimeAdapter.setDataList(getDataYearSelect(chooseTimeAdapter.dataList.first().year - COUNT_YEAR_BACK))
                }
            } else {
                chooseTimeAdapter.setDataList(getDataYearSelect(chooseTimeAdapter.dataList.last().year + COUNT_YEAR_NEXT))
            }
        }
    }

    private fun setSelectYear() {
        isSelectMonth = false
        val currentYearSelect = binding.tvYear.text.toString().toInt()
        val list = getDataYearSelect(currentYearSelect)
        list.find { it.year == currentYearSelect }?.isSelected = true
        chooseTimeAdapter.setDataList(list)
    }

    private fun getDataYearSelect(currentYearSelect: Int) = mutableListOf(
        ChooseTimeEntity("", currentYearSelect - 3, false, isSelected = false),
        ChooseTimeEntity("", currentYearSelect - 2, false, isSelected = false),
        ChooseTimeEntity("", currentYearSelect - 1, false, isSelected = false),
        ChooseTimeEntity("", currentYearSelect, false, isSelected = false),
        ChooseTimeEntity("", currentYearSelect + 1, false, isSelected = false),
        ChooseTimeEntity("", currentYearSelect + 2, false, isSelected = false),
        ChooseTimeEntity("", currentYearSelect + 3, false, isSelected = false),
        ChooseTimeEntity("", currentYearSelect + 4, false, isSelected = false),
        ChooseTimeEntity("", currentYearSelect + 5, false, isSelected = false),
        ChooseTimeEntity("", currentYearSelect + 6, false, isSelected = false),
        ChooseTimeEntity("", currentYearSelect + 7, false, isSelected = false),
        ChooseTimeEntity("", currentYearSelect + 8, false, isSelected = false)
    )
}