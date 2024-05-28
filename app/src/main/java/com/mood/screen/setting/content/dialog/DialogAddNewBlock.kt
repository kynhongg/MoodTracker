package com.mood.screen.setting.content.dialog

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.mood.R
import com.mood.databinding.DialogAddNewBlockBinding
import com.mood.utils.clear
import com.mood.utils.setOnSafeClick

class DialogAddNewBlock(val context: Context) {
    private val binding by lazy {
        DialogAddNewBlockBinding.inflate(LayoutInflater.from(context))
    }
    private val dialog: AlertDialog by lazy {
        AlertDialog.Builder(context, R.style.dialog_transparent_width)
            .setView(binding.root)
            .create()
    }

    fun isShowing() = dialog.isShowing
    fun hide() {
        binding.edtBlockName.clear()
        dialog.dismiss()
    }

    fun show(
        title: String? = null,
        blockName: String? = null,
        onClickAddBlock: ((String) -> Unit)? = null
    ) {
        title?.let {
            binding.tvTitle.text = it
        }
        blockName?.let {
            binding.edtBlockName.setText(it)
            binding.edtBlockName.setSelection(it.length)
            if (it.isNotEmpty()) {
                binding.btnAddBlock.isEnabled = true
                binding.btnAddBlock.setBackgroundResource(R.drawable.bg_button_done)
                binding.tvBtnDone.setTextColor(ContextCompat.getColor(context, R.color.white))
            } else {
                binding.btnAddBlock.isEnabled = false
                binding.btnAddBlock.setBackgroundResource(R.drawable.bg_button_done_disable)
                binding.tvBtnDone.setTextColor(ContextCompat.getColor(context, R.color.grey_default_text_start))
            }
        } ?: kotlin.run {
            binding.edtBlockName.clear()
        }
        binding.edtBlockName.doAfterTextChanged {
            if (it.toString().isNotEmpty()) {
                binding.btnAddBlock.isEnabled = true
                binding.btnAddBlock.setBackgroundResource(R.drawable.bg_button_done)
                binding.tvBtnDone.setTextColor(ContextCompat.getColor(context, R.color.white))
            } else {
                binding.btnAddBlock.isEnabled = false
                binding.btnAddBlock.setBackgroundResource(R.drawable.bg_button_done_disable)
                binding.tvBtnDone.setTextColor(ContextCompat.getColor(context, R.color.grey_default_text_start))
            }
        }
        binding.btnAddBlock.setOnSafeClick {
            val blockName = binding.edtBlockName.text.toString()
            if (blockName.isNotEmpty()) {
                onClickAddBlock?.invoke(blockName)
                hide()
            }
        }
        binding.layoutBtnClose.setOnSafeClick { hide() }
        binding.btnClose.setOnSafeClick { hide() }
        dialog.setCancelable(true)
        if (!isShowing()) {
            dialog.show()
        }
    }
}