package com.mood.utils

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.mood.R
import com.mood.data.entity.BeanDailyEntity
import com.mood.data.entity.BeanDefaultEmoji
import com.mood.data.entity.BeanIconEntity
import com.mood.data.entity.BeanImageAttachEntity
import com.mood.data.entity.BlockEmojiEntity
import com.mood.data.entity.IconEntity
import com.mood.screen.addbean.BeanTypeEntity
import com.mood.screen.relax.AmbientSound
import com.mood.screen.relax.SoundEntity
import com.mood.screen.relax.TriggerSound
import com.mood.screen.report.ChartEntry
import com.mood.screen.setting.content.entity.BlockIconDetailEntity
import com.mood.screen.setting.content.entity.IconBase
import com.mood.screen.timeline.BeanIconDetailEntity
import com.github.mikephil.charting.data.Entry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DataUtils {

    /**
     * return a list order like after:
     * [{blockName = "Weather"}, listIconByBlockId=[{IconEntity}]]
     **/
    fun orderIconByBlock(listBlock: MutableList<BlockEmojiEntity>, listIcon: MutableList<IconEntity>): MutableList<Any> {
        val dataList = mutableListOf<Any>()
        listBlock.sortedBy { it.blockOrder }.forEach { block ->
            if (block.blockIsShow) {
                dataList.add(block.blockName ?: "")
                val icons = listIcon.filter { it.iconBlockId == block.blockId && it.iconIsShow }.sortedBy { it.iconOrder }
                dataList.add(icons)
            }
        }
        Constant.orderIconByBlock = dataList
        return dataList
    }

    fun orderBlockIconDetail(
        listBlock: MutableList<BlockEmojiEntity>, listIcon: MutableList<IconEntity>,
        isShow: Boolean = true,
        isCheckIconShow: Boolean = false
    ): MutableList<Any> {
        val dataList = mutableListOf<Any>()
        val filteredBlocks = listBlock.filter { it.blockIsShow == isShow }.sortedBy { it.blockOrder }
        filteredBlocks.forEach { block ->
            dataList.add(block.blockName ?: "")
            val filteredIcons = if (isCheckIconShow) {
                listIcon.filter { it.iconBlockId == block.blockId && it.iconIsShow }.toMutableList()
            } else {
                listIcon.filter { it.iconBlockId == block.blockId }.toMutableList()
            }.sortedBy { it.iconOrder }
            val icons = filteredIcons.toMutableList()
            icons.add(IconEntity().apply {
                isTemp = true
            })
            dataList.add(BlockIconDetailEntity(block, icons))
        }
        return dataList
    }

    fun orderBlockIconDetail(
        listBlock: MutableList<BlockEmojiEntity>, listIcon: MutableList<IconEntity>
    ): List<BlockIconDetailEntity> {
        return listBlock.sortedBy { it.blockOrder }.map { block ->
            val icons = listIcon.filter {
                it.iconBlockId == block.blockId && it.iconIsShow
            }.sortedBy { it.iconOrder }
            BlockIconDetailEntity(block, icons)
        }
    }

    /**
     *  return a list bean type and set select with index (screen add_bean_activity)
     **/
    fun getDataBeanType(indexSelect: Int): List<BeanTypeEntity> {
        val emojiIndexSelect = indexSelect.coerceIn(1, BeanDefaultEmoji.values().size - 1)
        return (1 until BeanDefaultEmoji.values().size).map { index ->
            val isSelected = index == emojiIndexSelect
            BeanTypeEntity(BeanDefaultEmoji.values()[index], isSelected)
        }
    }

    /**
     *  return a list bean daily with icons, images attach
     **/
    fun orderBeanDailyDetail(
        listBean: MutableList<BeanDailyEntity>, listIcon: MutableList<IconEntity>,
        listBeanIcon: MutableList<BeanIconEntity>, listImageAttach: MutableList<BeanImageAttachEntity>
    ): List<BeanIconDetailEntity> {
        return listBean.map { bean ->
            val beanIcons = listBeanIcon.filter { item -> item.beanIconId == bean.beanIconId }
            val icons = listIcon.filter { item -> item.iconId in beanIcons.map { it.iconId } }
            val images = listImageAttach.filter { item -> item.beanId == bean.beanId }
            BeanIconDetailEntity(bean, icons, images)
        }
    }

    fun getTriggerSound(context: Context): List<SoundEntity> {
        return TriggerSound.values().map {
            val name = context.getString(it.soundNameId)
            SoundEntity(name, it.imageSource, it.url, isMute = false, isSelected = false)
        }
    }

    fun getAmbientSound(context: Context): List<SoundEntity> {
        val dataList = mutableListOf<SoundEntity>()
        AmbientSound.values().forEach {
            val name = context.getString(it.soundNameId)
            dataList.add(SoundEntity(name, it.imageSource, it.url, isMute = false, isSelected = false))
        }
        return dataList
    }

    private fun getFormatMinutes(hour: Int, minutes: Int, second: Int): String {
        var timeFormat = ""
        if (hour in 1..9) {
            timeFormat += "0${hour}h"
        } else if (hour > 10) {
            timeFormat += "${hour}h"
        }
        timeFormat += if (minutes < 10) {
            "0${minutes}m"
        } else {
            "${minutes}m"
        }
        timeFormat += if (second < 10) {
            "0${second}s"
        } else {
            "${second}s"
        }
        return timeFormat
    }

    fun getFormatMinutes(minutes: Int, second: Int): String {
        var timeFormat = ""
        timeFormat += if (minutes < 10) {
            "0$minutes:"
        } else {
            "$minutes:"
        }
        timeFormat += if (second < 10) {
            "0$second"
        } else {
            "$second"
        }
        return timeFormat
    }

    fun getFormatMinutes(second: Int): String {
        val minutes = second / 60
        val remainSecond = second - minutes * 60
        return getFormatMinutes(minutes, remainSecond)
    }

    fun getListIconBase(context: Context): MutableList<IconBase> {
        val list = mutableListOf<IconBase>()
        for (index in 0..75) {
            val name = "ic_icon_$index"
            val sourceEnableId = context.getDrawableIdByName(name)
            val sourceDisableId = context.getDrawableIdByName(name + "_off")
            list.add(IconBase(name, sourceEnableId, sourceDisableId))
        }
        return list
    }

    fun getBaseBeanType(context: Context) = mutableListOf(
        IconBase(
            context.getString(R.string.status_1), R.drawable.ic_bean_type_1, R.drawable
                .ic_bean_type_1_off, -1
        ),
        IconBase(
            context.getString(R.string.status_2), R.drawable.ic_bean_type_2, R.drawable
                .ic_bean_type_2_off, -2
        ),
        IconBase(
            context.getString(R.string.status_3), R.drawable.ic_bean_type_3, R.drawable
                .ic_bean_type_3_off, -3
        ),
        IconBase(
            context.getString(R.string.status_4), R.drawable.ic_bean_type_4, R.drawable
                .ic_bean_type_4_off, -4
        ),
        IconBase(
            context.getString(R.string.status_5), R.drawable.ic_bean_type_5, R.drawable
                .ic_bean_type_5_off, -5
        ),
        IconBase(
            context.getString(R.string.status_6), R.drawable.ic_bean_type_6, R.drawable
                .ic_bean_type_6_off, -6
        ),
        IconBase(
            context.getString(R.string.status_7), R.drawable.ic_bean_type_7, R.drawable
                .ic_bean_type_7_off, -7
        ),
        IconBase(
            context.getString(R.string.status_8), R.drawable.ic_bean_type_8, R.drawable
                .ic_bean_type_8_off, -8
        )
    )

    fun convertBeanToChartEntry(beanDailyEntity: BeanDailyEntity): Entry {
        return Entry(beanDailyEntity.day.toFloat(), ChartEntry.getYValueWithBeanType(beanDailyEntity.beanTypeId ?: 1))
    }

    fun getTimeFromSecond(totalSecond: Int): String {
        var remainSecond = totalSecond
        val hour = remainSecond / 3600
        remainSecond -= hour * 3600
        val minutes = remainSecond / 60
        remainSecond -= minutes * 60
        return getFormatMinutes(hour, minutes, remainSecond)
    }

    suspend fun retrieveImagesFromGallery(contentResolver: ContentResolver): List<Uri> = withContext(Dispatchers.IO) {
        val imageList = mutableListOf<Uri>()
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} ${Constant.SORT_DESC}"
        val query = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )
        query?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                imageList.add(contentUri)
                Log.d(Constant.TAG, "-----------------$contentUri")
                if (imageList.size < 100) {
                    if (Constant.mapImageGallery[contentUri] == null) {
                        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, contentUri)
                        val bitmapScale = Bitmap.createScaledBitmap(bitmap, 100, 100, false)
                        Constant.mapImageGallery[contentUri] = bitmapScale
                    }
                }
            }
        }
        return@withContext imageList
    }

}