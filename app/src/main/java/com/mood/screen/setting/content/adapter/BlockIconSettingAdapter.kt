package com.mood.screen.setting.content.adapter

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mood.R
import com.mood.data.entity.IconEntity
import com.mood.databinding.ItemTypeHeaderBinding
import com.mood.databinding.SettingItemBodyBlockIconBinding
import com.mood.screen.addbean.ItemType
import com.mood.screen.setting.content.entity.BlockIconDetailEntity
import com.mood.utils.inflateLayout
import com.mood.utils.setGridManager
import com.mood.utils.setOnSafeClick

class BlockIconSettingAdapter
@JvmOverloads constructor(
    dataList: MutableList<Any>? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var dataList: MutableList<Any> = dataList ?: arrayListOf()
        internal set
    var onClickIcon: ((blockId: Int, item: IconEntity, position: Int) -> Unit)? = null
    var onClickEditBlock: ((item: BlockIconDetailEntity) -> Unit)? = null
    var onClickViewBlock: ((item: BlockIconDetailEntity) -> Unit)? = null
    var onClickRemoveBlock: ((item: BlockIconDetailEntity) -> Unit)? = null

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: Any, position: Int) {
            val binding = ItemTypeHeaderBinding.bind(itemView)
            val blockName = item as String
            binding.tvBlockName.text = blockName
        }
    }

    inner class BodyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconAdapter by lazy {
            IconAdapterV2()
        }

        fun bind(item: Any, position: Int) {
            val binding = SettingItemBodyBlockIconBinding.bind(itemView)
            val block = item as BlockIconDetailEntity
            val alpha: Float
            val cardBackgroundColor: Int
            val iconViewOrHidden: Int
            if (item.blockEmojiEntity.blockIsShow) {
                alpha = 1f
                cardBackgroundColor = R.color.white
                iconViewOrHidden = R.drawable.ic_un_view
            } else {
                alpha = 0.7f
                cardBackgroundColor = R.color.grey_bg_hidden
                iconViewOrHidden = R.drawable.ic_view
            }
            binding.cardRcvIcon.alpha = alpha
            binding.cardRcvIcon.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, cardBackgroundColor))
            binding.action.btnView.setImageResource(iconViewOrHidden)
            binding.rcvIcon.setGridManager(binding.root.context, 5, iconAdapter)
            iconAdapter.setDataList(block.listIcon)
            binding.action.btnEdit.setOnSafeClick {
                onClickEditBlock?.invoke(block)
            }
            binding.action.btnView.setOnSafeClick {
                onClickViewBlock?.invoke(block)
            }
            binding.action.btnRemove.setOnSafeClick {
                onClickRemoveBlock?.invoke(block)
            }
            iconAdapter.setOnClickIcon { icon, positionIcon ->
                onClickIcon?.invoke(item.blockEmojiEntity.blockId, icon, positionIcon)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ItemType.Header.id -> {
                val view = parent.context.inflateLayout(R.layout.item_type_header, parent)
                HeaderViewHolder(view)
            }

            else -> {
                val view = parent.context.inflateLayout(R.layout.setting_item_body_block_icon, parent)
                BodyViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                holder.bind(dataList[position], position)
            }

            is BodyViewHolder -> {
                holder.bind(dataList[position], position)
            }
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (dataList[position]) {
            is String -> ItemType.Header.id
            is Int -> ItemType.Ads.id
            else -> ItemType.Body.id
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setDataList(data: Collection<Any>) {
        dataList.clear()
        dataList.addAll(data)
        notifyDataSetChanged()
    }
}