package com.mood.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "bean_image_attach")
class BeanImageAttachEntity : Serializable {
    @PrimaryKey(autoGenerate = true)
    var id = 0

    @ColumnInfo(name = "beanId")
    var beanId: Int? = null

    @ColumnInfo(name = "urlImage")
    var urlImage: String? = null
    override fun toString(): String {
        return "BeanImageAttachEntity(id=$id, beanId=$beanId, urlImage=$urlImage)"
    }
}