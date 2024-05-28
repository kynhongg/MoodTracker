package com.mood.screen.setting.content.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.mood.R
import com.mood.base.BaseAdapterRecyclerView
import com.mood.databinding.ItemBlockSettingBinding
import com.mood.screen.setting.content.entity.BlockIconDetailEntity
import com.mood.utils.Constant
import com.mood.utils.setGridManager
import com.mood.utils.showOrGone
import java.util.Collections


class SettingBlockAdapter : BaseAdapterRecyclerView<BlockIconDetailEntity, ItemBlockSettingBinding>(), ItemTouchHelperAdapter {
    var onUpdateMapStateIcon: ((iconId1: Int, iconId2: Int) -> Unit)? = null
    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) { //Thưc hiện đổi chỗ trong đoạn từ vị trí hiện tại đến vị trí đích
            for (i in fromPosition until toPosition) {
                val blockOrder1 = dataList[i].blockEmojiEntity.blockOrder
                val blockOrder2 = dataList[i + 1].blockEmojiEntity.blockOrder
                dataList[i].blockEmojiEntity.blockOrder = blockOrder2
                dataList[i + 1].blockEmojiEntity.blockOrder = blockOrder1
                Collections.swap(dataList, i, i + 1)
            }
            Log.d(Constant.TAG, "onMove: $dataList")
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                val blockOrder1 = dataList[i].blockEmojiEntity.blockOrder
                val blockOrder2 = dataList[i + 1].blockEmojiEntity.blockOrder
                dataList[i].blockEmojiEntity.blockOrder = blockOrder2
                dataList[i + 1].blockEmojiEntity.blockOrder = blockOrder1
                Collections.swap(dataList, i, i - 1)
            }
            Log.d(Constant.TAG, "onMove: $dataList")
        }
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun inflateBinding(inflater: LayoutInflater, parent: ViewGroup): ItemBlockSettingBinding {
        return ItemBlockSettingBinding.inflate(inflater, parent, false)
    }

    override fun bindData(binding: ItemBlockSettingBinding, item: BlockIconDetailEntity, position: Int) {
        val context = binding.root.context
        val settingIconAdapter = SettingIconAdapter()
        binding.rcvListIcon.setGridManager(context, 1, settingIconAdapter)
        settingIconAdapter.setDataList(item.listIcon)
        binding.tvBlockName.text = item.blockEmojiEntity.blockName
        binding.rcvListIcon.showOrGone(item.isVisibility)
        binding.btnExpand.setImageResource(
            if (item.isVisibility) {
                R.drawable.ic_arrow_down
            } else {
                R.drawable.ic_arrow_next
            }
        )
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, // Kéo lên và kéo xuống
            0 // Không hỗ trợ kéo sang trái hoặc phải
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition

                // Di chuyển mục từ vị trí ban đầu đến vị trí đích
                // Ở đây, bạn cần cập nhật danh sách dữ liệu của bạn
                val listIcon = item.listIcon
                if (fromPosition < toPosition) { //Thưc hiện đổi chỗ trong đoạn từ vị trí hiện tại đến vị trí đích
                    for (i in fromPosition until toPosition) {
                        val iconId1 = listIcon[i].iconId
                        val iconId2 = listIcon[i + 1].iconId
                        onUpdateMapStateIcon?.invoke(iconId1, iconId2)
                        Collections.swap(listIcon, i, i + 1)
                    }
                } else {
                    for (i in fromPosition downTo toPosition + 1) {
                        val iconId1 = listIcon[i].iconId
                        val iconId2 = listIcon[i - 1].iconId
                        onUpdateMapStateIcon?.invoke(iconId1, iconId2)
                        Collections.swap(listIcon, i, i - 1)
                    }
                }
                recyclerView.adapter?.notifyItemMoved(fromPosition, toPosition)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Không làm gì khi bị vuốt
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.rcvListIcon)
    }
}