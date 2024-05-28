package com.mood.screen.addbean

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import com.mood.R
import com.mood.databinding.DialogPickTimeSleepBinding
import com.mood.utils.CalendarUtil
import com.mood.utils.setOnSafeClick

class DialogPickTimeSleep(val context: Context) {
    private val binding by lazy {
        DialogPickTimeSleepBinding.inflate(LayoutInflater.from(context))
    }
    private val dialog: AlertDialog by lazy {
        AlertDialog.Builder(context, R.style.dialog_transparent_width).setView(binding.root)
            .create()
    }

    fun isShowing(): Boolean {
        return dialog.isShowing
    }

    fun hide() {
        dialog.dismiss()
    }

    fun show(
        title: String,
        isCancelAble: Boolean? = true,
        onClickSubmit: (hour: Int, minutes: Int) -> Unit
    ) {
        binding.hourPicker.apply {
            minValue = 0
            maxValue = 23
            setFormatter {
                String.format("%02d", it)
            }
        }
        binding.minutesPicker.apply {
            minValue = 0
            maxValue = 59
            setFormatter {
                String.format("%02d", it)
            }
        }
        isCancelAble?.let {
            dialog.setCancelable(isCancelAble)
        }
        binding.titleDialog.text = title
        binding.btnCancel.setOnSafeClick {
            hide()
        }
        binding.btnOk.setOnSafeClick {
            hide()
            onClickSubmit.invoke(binding.hourPicker.value, binding.minutesPicker.value)
        }
        if (!isShowing()) {
            dialog.show()
        }
    }

    fun showPickMonthYear(
        title: String,
        isCancelAble: Boolean? = true,
        month: Int,
        year: Int,
        onClickSubmit: (hour: Int, minutes: Int) -> Unit
    ) {
        binding.hourPicker.apply {
            minValue = 1
            maxValue = 12
            value = month
            wrapSelectorWheel = true
            displayedValues = CalendarUtil.getListMonthStrings(context).toTypedArray()
        }
        binding.minutesPicker.apply {
            minValue = 2000
            maxValue = 2100
            wrapSelectorWheel = true
            value = year
        }
        isCancelAble?.let {
            dialog.setCancelable(isCancelAble)
        }
        binding.titleDialog.text = title
        binding.btnCancel.setOnSafeClick {
            hide()
        }
        binding.btnOk.setOnSafeClick {
            hide()
            onClickSubmit.invoke(binding.hourPicker.value, binding.minutesPicker.value)
        }
        if (!isShowing()) {
            dialog.show()
        }
    }
}