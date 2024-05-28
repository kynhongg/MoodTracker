package com.mood.screen.setting.content.dialog

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.mood.R
import com.mood.databinding.DialogConfirmExitBinding
import com.mood.utils.setOnSafeClick

class DialogConfirmExit(val context: Context) {
    private val binding by lazy {
        DialogConfirmExitBinding.inflate(LayoutInflater.from(context))
    }
    private val dialog: AlertDialog by lazy {
        AlertDialog.Builder(context, R.style.dialog_transparent_width)
            .setView(binding.root)
            .create()
    }

    fun isShowing() = dialog.isShowing
    fun hide() {
        dialog.dismiss()
    }

    fun show(
        description: String? = null,
        tvButtonContinue: String? = null,
        onClickExit: () -> Unit
    ) {
        description?.let {
            binding.tvConfirmRemove.text = it
        }
        tvButtonContinue?.let {
            binding.tvButtonContinue.text = it
        }
        if (!isShowing()) {
            dialog.show()
        }
        dialog.setCancelable(true)
        binding.btnContinue.setOnSafeClick {
            hide()
            onClickExit.invoke()
        }
        binding.layoutBtnBlack.setOnSafeClick {
            hide()
        }
        binding.btnBack.setOnSafeClick {
            hide()
        }
    }
}