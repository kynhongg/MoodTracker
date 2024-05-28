package com.mood.screen.addbean

import android.content.Context
import android.view.LayoutInflater
import android.view.Window
import androidx.appcompat.app.AlertDialog
import com.mood.R
import com.mood.databinding.DialogAskBuyPremiumBinding

class GoPremiumDialog(val context: Context) {
    private val binding by lazy {
        DialogAskBuyPremiumBinding.inflate(LayoutInflater.from(context))
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
        binding.btnClose.setOnClickListener { dialog.dismiss() }
        binding.btnGoPremium.setOnClickListener {
            onClickDone?.invoke()
            dialog.dismiss()
        }
        dialog.setCancelable(false)

        if (!dialog.isShowing)
            dialog.show()
    }

}