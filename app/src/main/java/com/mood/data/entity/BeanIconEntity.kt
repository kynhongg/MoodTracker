package com.mood.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "bean_icons")
class BeanIconEntity : Serializable {
    @PrimaryKey(autoGenerate = true)
    var id = 0

    @ColumnInfo(name = "beanIconId")
    var beanIconId: Int? = null

    @ColumnInfo(name = "iconId")
    var iconId: Int? = null
    override fun toString(): String {
        return "BeanIcon(id=$id, beanIconId=$beanIconId, iconId=$iconId)"
    }
}