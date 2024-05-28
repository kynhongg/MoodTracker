package com.mood.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "music_calms")
class MusicCalmEntity : Serializable {
    @PrimaryKey(autoGenerate = true)
    var calmId: Int = 0

    @ColumnInfo(name = "calm_second")
    var second: Int = 0

    @ColumnInfo(name = "calm_month")
    var month: Int = 0

    @ColumnInfo(name = "calm_year")
    var year: Int = 0
}