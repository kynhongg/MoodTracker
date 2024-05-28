package com.mood.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "icons")
data class IconEntity(
    @PrimaryKey(autoGenerate = true)
    var iconId: Int = 0,

    @ColumnInfo(name = "icon_block_id")
    var iconBlockId: Int? = null,

    @ColumnInfo(name = "icon_name")
    var iconName: String? = null,

    @ColumnInfo(name = "icon_url")
    var iconUrl: String? = null,

    @ColumnInfo(name = "icon_is_show")
    var iconIsShow: Boolean = true,

    @ColumnInfo(name = "icon_order")
    var iconOrder: Int = 1,

    @Ignore
    var isSelected: Boolean = false,

    @Ignore
    var isTemp: Boolean = false

) : Serializable {
    override fun toString(): String {
        return "IconEntity(iconId=$iconId, iconName=$iconName, iconUrl=$iconUrl, iconOrder=$iconOrder)\n"
    }

    fun compareOrder(other: IconEntity): Boolean {
        return iconId == other.iconId
                && iconOrder == other.iconOrder
    }
}