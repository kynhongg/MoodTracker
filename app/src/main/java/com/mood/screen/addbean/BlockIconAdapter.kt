package com.mood.screen.addbean

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mood.R
import com.mood.data.entity.IconEntity
import com.mood.databinding.ItemBodyBlockIconBinding
import com.mood.databinding.ItemTypeHeaderBinding
import com.mood.utils.inflateLayout
import com.mood.utils.setGridManager

class BlockIconAdapter
@JvmOverloads constructor(
    dataList: MutableList<Any>? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var dataList: MutableList<Any> = dataList ?: arrayListOf()
        internal set
    private var onClickItem: ((item: IconEntity, position: Int) -> Unit)? = null

    fun setOnClickItem(listener: (item: IconEntity, position: Int) -> Unit) {
        onClickItem = listener
    }

    var listIconEntity = mutableListOf<IconEntity>()

    fun getAllIconSelected(): List<IconEntity> = listIconEntity.filter { it.isSelected }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: Any, position: Int) {
            val binding = ItemTypeHeaderBinding.bind(itemView)
            val blockName = item as String
            binding.tvBlockName.text = blockName
        }
    }

    inner class BodyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val NUMBER_ICON_PER_LINE = 4
        private val iconAdapter by lazy {
            IconAdapter()
        }

        fun bind(item: Any, position: Int) {
            val binding = ItemBodyBlockIconBinding.bind(itemView)
            val listIcon = item as List<IconEntity>
            binding.rcvIcon.setGridManager(binding.root.context, NUMBER_ICON_PER_LINE, iconAdapter)
            iconAdapter.setDataList(listIcon)
            iconAdapter.setOnClickIcon { icon, positionIcon ->
                iconAdapter.setSelectedItem(positionIcon)
                setSelectedIcon(icon.iconId, icon)
                onClickItem?.invoke(icon, positionIcon)
            }
        }
    }

    private fun setSelectedIcon(iconId: Int, icon: IconEntity) {
        val index = listIconEntity.indexOfFirst { it.iconId == iconId }
        if (index != -1) {
            listIconEntity[index].isSelected = icon.isSelected
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ItemType.Header.id -> {
                val view = parent.context.inflateLayout(R.layout.item_type_header, parent)
                HeaderViewHolder(view)
            }

            else -> {
                val view = parent.context.inflateLayout(R.layout.item_body_block_icon, parent)
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
        listIconEntity.clear()
        dataList.forEach { item ->
            if (item !is String && item !is Int) {
                val icons = item as List<IconEntity>
                listIconEntity.addAll(icons)
            }
        }
        notifyDataSetChanged()
    }
}