package com.mood.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "blocks")
data class BlockEmojiEntity(
    @PrimaryKey(autoGenerate = true)
    var blockId: Int = 0,
    @ColumnInfo(name = "block_name")
    var blockName: String? = null,
    @ColumnInfo(name = "block_is_show")
    var blockIsShow: Boolean = true,
    @ColumnInfo(name = "block_order")
    var blockOrder: Int = 1,
    @Ignore
    var isSelected: Boolean = false
) : Serializable {
    override fun toString(): String {
        return "BlockEmojiEntity(blockId=$blockId, blockName=$blockName, blockIsShow=$blockIsShow, blockOrder=$blockOrder)"
    }

    fun compareOrder(other: BlockEmojiEntity): Boolean {
        return blockId == other.blockOrder
                && blockOrder == other.blockOrder
    }
}