package com.mood.screen.setting.content.dialog

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.mood.R
import com.mood.databinding.DialogAddNewIconBinding
import com.mood.utils.clear
import com.mood.utils.getDrawableIdByName
import com.mood.utils.setOnSafeClick

class DialogAddNewIcon(val context: Context) {
    private val binding by lazy {
        DialogAddNewIconBinding.inflate(LayoutInflater.from(context))
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

    private var iconNameSelected = ""
    fun show(
        iconName: String? = null,
        iconUrl: String? = null,
        onClickAddIcon: ((iconName: String, iconSource: String) -> Unit)? = null
    ) {
        binding.btnAddIcon.isEnabled = false
        iconName?.let {
            binding.edtBlockName.setText(it)
            binding.edtBlockName.setSelection(it.length)
        }
        iconUrl?.let {
            binding.imgIconNew.setImageResource(context.getDrawableIdByName(it))
            iconNameSelected = it
        } ?: kotlin.run {
            binding.imgIconNew.setImageResource(R.drawable.background_default_sound)
        }
        if (binding.edtBlockName.text.isNotEmpty() && iconNameSelected.isNotEmpty() &&
            (iconNameSelected != iconUrl || binding.edtBlockName.text.toString() != iconName)
        ) {
            binding.btnAddIcon.isEnabled = true
        }
        binding.edtBlockName.doAfterTextChanged {
            if (it.toString().isNotEmpty() && iconNameSelected.isNotEmpty()
                && (iconNameSelected != iconUrl || it.toString() != iconName)
            ) {
                binding.btnAddIcon.isEnabled = true
                binding.btnAddIcon.setBackgroundResource(R.drawable.bg_button_done)
                binding.tvBtnDone.setTextColor(ContextCompat.getColor(context, R.color.white))
            } else {
                binding.btnAddIcon.isEnabled = false
                binding.btnAddIcon.setBackgroundResource(R.drawable.bg_button_done_disable)
                binding.tvBtnDone.setTextColor(ContextCompat.getColor(context, R.color.grey_default_text_start))
            }
        }
        binding.btnAddIcon.setOnSafeClick {
            val iconName = binding.edtBlockName.text.toString()
            if (iconName.isNotEmpty()) {
                onClickAddIcon?.invoke(iconName, iconNameSelected)
                iconNameSelected = ""
                binding.edtBlockName.text.clear()
                hide()
            }
        }
        binding.imgIconNew.setOnSafeClick {
            DialogPickIconBase(context).also { alert ->
                alert.show(
                    iconUrl?.let { it1 -> context.getDrawableIdByName(it1) }
                        ?: R.drawable.background_default_sound
                ) { iconBase ->
                    binding.imgIconNew.setImageResource(iconBase.sourceId)
                    iconNameSelected = iconBase.name
                    if (binding.edtBlockName.text.isNotEmpty()) {
                        binding.btnAddIcon.isEnabled = true
                        binding.btnAddIcon.setBackgroundResource(R.drawable.bg_button_done)
                        binding.tvBtnDone.setTextColor(ContextCompat.getColor(context, R.color.white))
                    }
                }
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