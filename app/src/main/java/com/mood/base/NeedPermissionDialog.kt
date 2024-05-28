package com.mood.base

import android.content.Context
import android.view.LayoutInflater
import android.view.Window
import androidx.appcompat.app.AlertDialog
import com.mood.R
import com.mood.databinding.DialogNeedPermissionBinding

class NeedPermissionDialog(val context: Context) {
    private val binding by lazy {
        DialogNeedPermissionBinding.inflate(LayoutInflater.from(context))
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
        title: String? = null,
        description: String? = null,
        onClickDone: (() -> Unit)? = null,
        onCLickCLose: (() -> Unit)? = null
    ) {
        title?.let {
            binding.title.text = it
        }
        description?.let {
            binding.description.text = it
        }
        binding.btnBack.setOnClickListener {
            dialog.dismiss()
            onCLickCLose?.invoke()
        }
        binding.btnAllow.setOnClickListener {
            onClickDone?.invoke()
            dialog.dismiss()
        }
        dialog.setCancelable(false)

        if (!dialog.isShowing)
            dialog.show()
    }

}