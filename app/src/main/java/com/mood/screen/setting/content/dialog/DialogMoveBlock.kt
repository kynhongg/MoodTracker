package com.mood.screen.setting.content.dialog

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.WindowManager
import com.mood.R
import com.mood.data.entity.BlockEmojiEntity
import com.mood.databinding.DialogMoveBlockBinding
import com.mood.screen.setting.content.adapter.ListBlockAdapter
import com.mood.utils.setGridManager
import com.mood.utils.setOnSafeClick

class DialogMoveBlock(val context: Context) {
    private val binding by lazy {
        DialogMoveBlockBinding.inflate(LayoutInflater.from(context))
    }
    private val dialog: AlertDialog by lazy {
        AlertDialog.Builder(context, R.style.dialog_full_width_height)
            .setView(binding.root)
            .create()
    }
    private val listBlockAdapter by lazy {
        ListBlockAdapter()
    }

    fun isShowing() = dialog.isShowing
    fun hide() {
        dialog.dismiss()
    }

    private var itemSelected: Int? = null

    fun show(
        blockId: Int,
        dataList: List<BlockEmojiEntity>,
        onClickDone: ((blockIdSelect: Int?) -> Unit)? = null
    ) {
        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        binding.rcvListBlock.setGridManager(context, 1, adapter = listBlockAdapter)
        listBlockAdapter.setDataList(dataList)
        itemSelected = dataList.firstOrNull()?.blockId
        val index = dataList.indexOfFirst { it.blockId == blockId }
        listBlockAdapter.setSelectedItem(index)
        listBlockAdapter.setOnClickItem { item, position ->
            listBlockAdapter.setSelectedItem(position)
            itemSelected = item?.blockId
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