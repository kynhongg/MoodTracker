package com.mood.screen.sync

import android.content.Context
import android.view.LayoutInflater
import android.view.Window
import androidx.appcompat.app.AlertDialog
import com.mood.R
import com.mood.databinding.DialogLoadingBinding
import com.mood.utils.hide
import com.mood.utils.show

class LoadingDialog(val context: Context) {
    private val binding by lazy {
        DialogLoadingBinding.inflate(LayoutInflater.from(context))
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

    fun show(onClickDone: (() -> Unit)? = null) {
        binding.btnBack.setOnClickListener { dialog.dismiss() }
        binding.btnClose.setOnClickListener {
            onClickDone?.invoke()
            dialog.dismiss()
        }
        dialog.setCancelable(false)

        if (!dialog.isShowing)
            dialog.show()
    }

    fun setViewWithState(state: Boolean) {
        if (state) {
            binding.title.text = context.getString(R.string.sync_data)
            binding.btnClose.show()
            binding.loading.setAnimation(R.raw.sync_done)
        } else {
            binding.title.text = context.getString(R.string.txt_synchronizing)
            binding.btnClose.hide()
            binding.loading.setAnimation(R.raw.cloud)
            binding.loading.repeatCount = 9999
            binding.loading.playAnimation()
        }
    }

    fun setError() {
        binding.title.text = context.getString(R.string.txt_sync_failed)
        binding.btnClose.show()
        binding.imageError.show()
    }

}