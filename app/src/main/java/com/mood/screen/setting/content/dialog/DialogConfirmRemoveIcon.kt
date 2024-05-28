package com.mood.screen.setting.content.dialog

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.mood.R
import com.mood.databinding.DialogConfirmRemoveIconBinding
import com.mood.utils.setOnSafeClick

class DialogConfirmRemoveIcon(val context: Context) {
    private val binding by lazy {
        DialogConfirmRemoveIconBinding.inflate(LayoutInflater.from(context))
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
        msg: String? = null,
        onClickRemove: () -> Unit
    ) {
        msg?.let {
            binding.tvConfirmRemove.text = it
        }
        binding.btnCancel.setOnSafeClick { hide() }
        binding.btnDelete.setOnSafeClick {
            hide()
            onClickRemove.invoke()
        }
        dialog.setCancelable(true)
        if (!isShowing()) {
            dialog.show()
        }
    }
}