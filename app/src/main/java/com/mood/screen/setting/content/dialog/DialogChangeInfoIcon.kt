package com.mood.screen.setting.content.dialog

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.mood.R
import com.mood.databinding.DialogChangeInfoIconBinding
import com.mood.utils.getDrawableIdByName
import com.mood.utils.setOnSafeClick

class DialogChangeInfoIcon(val context: Context) {
    private val binding by lazy {
        DialogChangeInfoIconBinding.inflate(LayoutInflater.from(context))
    }
    private val dialog: AlertDialog by lazy {
        AlertDialog.Builder(context, R.style.dialog_transparent_width)
            .setView(binding.root)
            .create()
    }
    private val dialogConfirmRemove by lazy {
        DialogConfirmRemoveIcon(context)
    }

    fun isShowing() = dialog.isShowing
    fun hide() {
        dialog.dismiss()
    }

    fun show(
        iconName: String,
        iconUrl: String,
        iconIsShow: Boolean = true,
        onClickEditIcon: (() -> Unit)? = null,
        onClickMoveBlock: () -> Unit,
        onClickHideShowIcon: () -> Unit,
        onClickRemoveIcon: () -> Unit
    ) {
        binding.tvIconName.text = iconName
        binding.tvHideShowIcon.text = if (iconIsShow) {
            context.getString(R.string.hide_from_record)
        } else {
            context.getString(R.string.show_from_record)
        }
        binding.imgIcon.setImageResource(context.getDrawableIdByName(iconUrl))
        binding.btnEditIcon.setOnSafeClick {
            hide()
            onClickEditIcon?.invoke()
        }
        binding.imgIcon.setOnSafeClick {
            hide()
            onClickEditIcon?.invoke()
        }
        binding.layoutMoveToDifferentBlock.setOnSafeClick {
            hide()
            onClickMoveBlock.invoke()
        }
        binding.layoutHideShowFromRecord.setOnSafeClick {
            hide()
            onClickHideShowIcon.invoke()
        }
        binding.layoutRemoveIcon.setOnSafeClick {
            if (!dialogConfirmRemove.isShowing()) {
                dialogConfirmRemove.show {
                    hide()
                    onClickRemoveIcon.invoke()
                }
            }
        }
        dialog.setCancelable(true)
        if (!isShowing()) {
            dialog.show()
        }
    }
}