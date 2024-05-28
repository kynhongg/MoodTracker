package com.mood.base

import android.content.Context
import android.view.LayoutInflater
import android.view.Window
import androidx.appcompat.app.AlertDialog
import com.mood.R
import com.mood.databinding.DialogAlertRateAppBinding
import com.mood.utils.setOnSafeClick

class RateAppDialog(val context: Context) {
    private val binding by lazy {
        DialogAlertRateAppBinding.inflate(LayoutInflater.from(context))
    }
    private val dialog: AlertDialog by lazy {
        AlertDialog.Builder(context, R.style.dialog_transparent_width).setView(binding.root)
            .create()
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
        cancelAble: Boolean? = true,
        onClickSubmit: ((isLike: Boolean) -> Unit)? = null,
        onClickClose: (() -> Unit)? = null
    ) {

        binding.btnLike.setOnSafeClick {
            onClickSubmit?.invoke(true)
            hide()
        }
        binding.btnDislike.setOnSafeClick {
            onClickSubmit?.invoke(false)
            hide()
        }
        binding.btnClose.setOnSafeClick {
            onClickClose?.invoke()
            hide()
        }

        dialog.setCancelable(cancelAble ?: false)

        if (!dialog.isShowing)
            dialog.show()
    }

}