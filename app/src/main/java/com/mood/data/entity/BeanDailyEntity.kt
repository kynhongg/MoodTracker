package com.mood.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "beans")
class BeanDailyEntity : Serializable {
    @PrimaryKey(autoGenerate = true)
    var beanId = 0

    //happy, sad, angry, v...
    @ColumnInfo(name = "bean_type_id")
    var beanTypeId: Int? = null

    @ColumnInfo(name = "bean_year_create")
    var year: Int = 0

    @ColumnInfo(name = "bean_month_create")
    var month: Int = 0

    @ColumnInfo(name = "bean_day_create")
    var day: Int = 0

    @ColumnInfo(name = "bean_hour_create")
    var hour: Int = 0

    @ColumnInfo(name = "bean_minutes_create")
    var minutes: Int = 0

    //list of emoji
    @ColumnInfo(name = "bean_icon_id")
    var beanIconId: Int? = null

    @ColumnInfo(name = "bean_description")
    var beanDescription: String? = null

    @ColumnInfo(name = "bean_time_go_to_bed")
    var timeGoToBed: String? = null

    @ColumnInfo(name = "bean_time_wake_up")
    var timeWakeup: String? = null
    override fun toString(): String {
        return "BeanDailyEntity(beanId=$beanId, beanTypeId=$beanTypeId, year=$year, month=$month, day=$day, hour=$hour, minutes=$minutes, beanIconId=$beanIconId, beanDescription=$beanDescription, timeGoToBed=$timeGoToBed, timeWakeup=$timeWakeup)"
    }

}