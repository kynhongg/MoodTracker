package com.mood.screen.setting.content.dialog

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.mood.R
import com.mood.databinding.DialogPickIconBinding
import com.mood.screen.setting.content.adapter.ListIconAdapter
import com.mood.screen.setting.content.entity.IconBase
import com.mood.utils.DataUtils
import com.mood.utils.setGridManager
import com.mood.utils.setOnSafeClick

class DialogPickIconBase(val context: Context) {
    private val binding by lazy {
        DialogPickIconBinding.inflate(LayoutInflater.from(context))
    }
    private val dialog: AlertDialog by lazy {
        AlertDialog.Builder(context, R.style.dialog_full_width_height)
            .setView(binding.root)
            .create()
    }
    private val listIconAdapter by lazy {
        ListIconAdapter()
    }

    fun isShowing() = dialog.isShowing
    fun hide() {
        dialog.dismiss()
    }

    private var itemSelected = IconBase("ic_icon_5", R.drawable.ic_icon_5)

    fun show(
        sourceId: Int? = null,
        onClickDone: ((IconBase) -> Unit
        )? = null
    ) {
        sourceId?.let {
            binding.imgIcon1.setImageResource(sourceId)
        }
//        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        binding.rcvListIcon.setGridManager(context, 6, adapter = listIconAdapter)
        listIconAdapter.setDataList(DataUtils.getListIconBase(context))
        listIconAdapter.setOnClickItem { item, position ->
            item?.sourceId?.let {
                binding.imgIcon2.setImageResource(it)
                itemSelected = item
            }
        }
        binding.layoutBtnClose.setOnSafeClick { hide() }
        binding.btnClose.setOnSafeClick { hide() }
        binding.btnDonePickIcon.setOnSafeClick {
            onClickDone?.invoke(itemSelected)
            hide()
        }
        dialog.setCancelable(true)
        if (!isShowing()) {
            dialog.show()
        }
    }
}