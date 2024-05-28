package com.mood.data.database

import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.RelativeSizeSpan
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.PieEntry
import com.mood.data.entity.BeanDailyEntity
import com.mood.data.entity.BeanIconEntity
import com.mood.data.entity.BeanImageAttachEntity
import com.mood.data.entity.BlockEmojiEntity
import com.mood.data.entity.IconEntity
import com.mood.data.entity.MusicCalmEntity
import com.mood.screen.report.IconRank
import com.mood.screen.report.IconRankEntity
import com.mood.utils.CalendarUtil
import com.mood.utils.Constant
import com.mood.utils.isSdkQ
import kotlinx.coroutines.launch
import java.io.OutputStream
import kotlin.math.abs

class BeanViewModel(private val repository: BeanRepository) : ViewModel() {
    var isFilter: Boolean = false
    val allBeans: LiveData<List<BeanDailyEntity>> = repository.allBean.asLiveData()
    val allBlocks: LiveData<List<BlockEmojiEntity>> = repository.allBlock.asLiveData()
    val allIcons: LiveData<List<IconEntity>> = repository.allIcons.asLiveData()
    val allBeanIcon: LiveData<List<BeanIconEntity>> = repository.allBeanIcon.asLiveData()
    val allBeanImageAttach: LiveData<List<BeanImageAttachEntity>> =
        repository.allBeanImageAttachEntity.asLiveData()

    fun getAllData() {
        getAllBean()
        getAllIcon()
        getAllBlock()
    }

    //beans
    fun getAllBean(onSuccess: ((List<BeanDailyEntity>) -> Unit)? = null) = viewModelScope.launch {
        onSuccess?.invoke(repository.getAllBeans())
    }

    fun getMaxBeanIconId(onSuccess: ((beanIconId: Long) -> Unit)? = null) = viewModelScope.launch {
        onSuccess?.invoke(repository.getMaxBeanIconId())
    }

    fun insertBean(beanDailyEntity: BeanDailyEntity, onSuccess: ((beanId: Long) -> Unit)? = null) =
        viewModelScope.launch {
            onSuccess?.invoke(repository.insertBean(beanDailyEntity))
        }

    fun insertBean(beanDailyEntity: BeanDailyEntity) = viewModelScope.launch {
        repository.insertBean(beanDailyEntity)
    }

    fun updateBean(beanDailyEntity: BeanDailyEntity) = viewModelScope.launch {
        repository.updateBean(beanDailyEntity)
    }

    fun deleteBeanById(beanId: Int, onSuccess: (() -> Unit)? = null) = viewModelScope.launch {
        repository.deleteBean(beanId)
    }

    //icons
    fun getAllIcon(onSuccess: ((List<IconEntity>) -> Unit)? = null) = viewModelScope.launch {
        onSuccess?.invoke(repository.getAllIcon())
    }

    fun insertIcon(iconEntity: IconEntity) = viewModelScope.launch {
        repository.insertIcon(iconEntity)
    }

    fun updateIcon(iconEntity: IconEntity) = viewModelScope.launch {
        repository.updateIcon(iconEntity)
    }

    fun deleteIcon(iconId: Int) = viewModelScope.launch {
        repository.deleteIcon(iconId)
    }

    fun deleteIconByBlockId(blockId: Int) = viewModelScope.launch {
        repository.deleteIconByBlockId(blockId)
    }

    fun deleteAllIcon() = viewModelScope.launch {
        repository.deleteAllIcon()
    }

    //blocks
    fun getAllBlock(onSuccess: ((List<BlockEmojiEntity>) -> Unit)? = null) = viewModelScope.launch {
        onSuccess?.invoke(repository.getAllBlock())
    }

    fun insertBlock(blockEmojiEntity: BlockEmojiEntity) = viewModelScope.launch {
        repository.insertBlock(blockEmojiEntity)
    }

    fun deleteBlock(blockId: Int) = viewModelScope.launch {
        repository.deleteBlock(blockId)
    }

    fun updateBlock(blockEmojiEntity: BlockEmojiEntity) = viewModelScope.launch {
        repository.updateBlock(blockEmojiEntity)
    }

    //bean_icons
    fun insertBeanIcon(beanIconEntity: BeanIconEntity) = viewModelScope.launch {
        repository.insertBeanIcon(beanIconEntity)
    }

    fun getAllBeanIconById(id: Int, onSuccess: ((List<BeanIconEntity>) -> Unit)? = null) =
        viewModelScope.launch {
            onSuccess?.invoke(repository.getAllBeanIconById(id))
        }

    fun getAllBeanIcon(onSuccess: ((List<BeanIconEntity>) -> Unit)? = null) =
        viewModelScope.launch {
            onSuccess?.invoke(repository.getAllBeanIcon())
        }

    fun deleteBeanIconByIconAndBean(iconId: Int, beanIconId: Int) = viewModelScope.launch {
        repository.deleteBeanIconByIconAndBean(iconId, beanIconId)
    }

    fun deleteBeanIconByIconId(iconId: Int) = viewModelScope.launch {
        repository.deleteBeanIconByIconId(iconId)
    }

    fun deleteBeanIconByBeanIconId(beanIconId: Int) = viewModelScope.launch {
        repository.deleteBeanIconByBeanIconId(beanIconId)
    }

    //bean attach image
    fun getAllBeanImageAttachById(
        beanId: Int,
        onSuccess: ((List<BeanImageAttachEntity>) -> Unit)? = null
    ) = viewModelScope
        .launch {
            onSuccess?.invoke(repository.getAllBeanImageAttach(beanId))
        }

    fun getAllBeanImageAttach(onSuccess: ((List<BeanImageAttachEntity>) -> Unit)? = null) =
        viewModelScope.launch {
            onSuccess?.invoke(repository.getAllBeanImageAttach())
        }

    fun insertBeanImageAttach(beanImageAttachEntity: BeanImageAttachEntity) =
        viewModelScope.launch {
            repository.insertBeanImageAttach(beanImageAttachEntity)
        }

    fun deleteBeanImageAttachById(beanId: Int, onSuccess: ((Int) -> Unit)) = viewModelScope.launch {
        onSuccess.invoke(
            repository.deleteBeanImageAttachById(beanId)
        )
    }

    //music calm
    fun getAllMusicCalm(onSuccess: ((List<MusicCalmEntity>) -> Unit)? = null) =
        viewModelScope.launch {
            onSuccess?.invoke(repository.getAllMusicCalm())
        }

    fun getSecondMusicCalmWithMonth(month: Int, year: Int, onSuccess: ((second: Int) -> Unit)) =
        viewModelScope.launch {
            onSuccess.invoke(repository.getSecondCalmWithMonth(month, year))
        }

    fun insertMusicCalm(musicCalmEntity: MusicCalmEntity) = viewModelScope.launch {
        repository.insertMusicCalm(musicCalmEntity)
    }

    fun updateMusicCalm(musicCalmEntity: MusicCalmEntity) = viewModelScope.launch {
        repository.updateMusicCalm(musicCalmEntity)
    }

    fun updateSecondCalmWithMonthYear(second: Int, month: Int, year: Int) = viewModelScope.launch {
        repository.updateSecondCalmWithMonthYear(second, month, year)
    }

    fun insertSharingImage(contentResolver: ContentResolver, bitmap: Bitmap): Uri? {
        val name = "${System.currentTimeMillis()}"
        return insertImage(contentResolver, bitmap, name)
    }

    private fun insertImage(resolver: ContentResolver, bitmap: Bitmap, fileName: String): Uri? {
        val fos: OutputStream?
//        val APP_MEDIA_FOLDER = "doan"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, Constant.TYPE_JPEG)
            if (isSdkQ()) { // this one
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES
                )
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let { resolver.openOutputStream(it) }.also { fos = it }
        fos?.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        fos?.flush()
        fos?.close()
        contentValues.clear()
        if (isSdkQ()) {
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
            uri?.let {
                resolver.update(it, contentValues, null, null)
            }
        }
        return uri
    }

    private val listType = listOf(1, 2, 3, 4, 5, 6, 7, 8)
    private val xanhThan = Color.parseColor("#8488AD")
    private val xanhBlue = Color.parseColor("#60A6E1")
    private val red = Color.parseColor("#FF4D4D")
    private val pink = Color.parseColor("#FE79A6")
    private val grey = Color.parseColor("#9D9FAD")
    private val yellow = Color.parseColor("#FFD55C")
    private val orange = Color.parseColor("#FF804B")
    private val xanhla = Color.parseColor("#35C972")
    private val listColorType = listOf(
        red, xanhThan, orange, xanhBlue, grey, yellow, pink, xanhla
    )

    //get entry pie chart with list bean
    fun getEntriesPieChart(beans: List<BeanDailyEntity>): MutableList<PieEntry> {
        val pieEntry = mutableListOf<PieEntry>()
        listType.forEach { type ->
            val number = beans.count { it.beanTypeId == type }
            if (number > 0) {
                pieEntry.add(PieEntry(beans.count { it.beanTypeId == type }.toFloat()))
            }
        }
        return pieEntry
    }

    //get color pie chart with list bean
    fun getColorPieChart(beans: List<BeanDailyEntity>): MutableList<Int> {
        val listColor = mutableListOf<Int>()
        listType.forEachIndexed { index, type ->
            if (beans.any { it.beanTypeId == type }) {
                listColor.add(listColorType[index])
            }
        }
        return listColor
    }

    fun getCenterTextPieChartFormat(text1: String, text2: String): SpannableStringBuilder {
        return SpannableStringBuilder(text1 + text2).apply {
            setSpan(
                RelativeSizeSpan(2.25f), 0, text1.length, Spannable
                    .SPAN_INCLUSIVE_INCLUSIVE
            )
        }
    }

    fun getDataListIconRanking(
        listIconEntity: List<IconEntity>,
        listBeanIconEntity: List<BeanIconEntity>,
        listBeanDailyEntity: List<BeanDailyEntity>,
        blockId: Int? = null,
        month: Int,
        year: Int
    ): List<IconRankEntity> {
        val dataList = mutableListOf<IconRankEntity>()
        val newYear = if (month > 1) year else year - 1
        val newMonth = if (month > 1) month - 1 else 12
        val beans = listBeanDailyEntity.filter { it.month == month && it.year == year }
        val beansPrevious =
            listBeanDailyEntity.filter { it.month == newMonth && it.year == newYear }
        val beanIconIds = beans.map { it.beanIconId }
        val beanIconIdsPrevious = beansPrevious.map { it.beanIconId }
        val beanIcons = listBeanIconEntity.filter { it.beanIconId in beanIconIds }
        val beanIconsPrevious = listBeanIconEntity.filter { it.beanIconId in beanIconIdsPrevious }

        val filteredListIconEntity = if (blockId != null) {
            listIconEntity.filter { it.iconBlockId == blockId }
        } else {
            listIconEntity
        }

        filteredListIconEntity.forEachIndexed { index, iconEntity ->
            dataList.add(
                convertIconToRank(iconEntity, beanIcons, beanIconsPrevious, index)
            )
        }

        return dataList
    }

    private fun convertIconToRank(
        iconEntity: IconEntity,
        beanIcons: List<BeanIconEntity>,
        beanIconsPrevious: List<BeanIconEntity>,
        index: Int
    ): IconRankEntity {
        val iconCountCurrentMonth = getCountIconInMonthYear(iconEntity.iconId, beanIcons)
        val iconCountPreviousMonth = getCountIconInMonthYear(iconEntity.iconId, beanIconsPrevious)
        val iconDiff = abs(iconCountCurrentMonth - iconCountPreviousMonth)

        val iconCompareType = when {
            iconCountCurrentMonth > iconCountPreviousMonth -> IconRank.Top
            iconCountCurrentMonth < iconCountPreviousMonth -> IconRank.Down
            else -> IconRank.Equal
        }

        return IconRankEntity(
            index + 1, iconEntity.iconName ?: "",
            iconEntity.iconUrl ?: "", iconCountCurrentMonth, iconDiff, iconCompareType
        )
    }

    private fun getCountIconInMonthYear(iconId: Int, beanIcons: List<BeanIconEntity>): Int {
        return beanIcons.count { it.iconId == iconId }
    }

    fun getAvgBedTime(beans: List<BeanDailyEntity>): Int {
        return CalendarUtil.getHourAverage(beans.filter { it.timeGoToBed != null }
            .map { it.timeGoToBed ?: "00:00" })
    }

    fun getAvgWakeTime(beans: List<BeanDailyEntity>): Int {
        return CalendarUtil.getHourAverage(beans.filter { it.timeWakeup != null }
            .map { it.timeWakeup ?: "00:00" })
    }

    fun getAvgSleepTime(beans: List<BeanDailyEntity>): Int {
        val listTime = beans.filter { it.timeGoToBed != null && it.timeWakeup != null }
        if (listTime.isEmpty()) {
            return 0
        }
        var sum = 0
        listTime.forEach {
            val bedTime = CalendarUtil.convertStringToHour(it.timeGoToBed ?: "00:00")
            val wakeTime = CalendarUtil.convertStringToHour(it.timeWakeup ?: "00:00")
            sum += if (bedTime > wakeTime) {//wake time is tomorrow
                1440 - (bedTime - wakeTime)
            } else {
                wakeTime - bedTime
            }
        }
        return sum / listTime.size
    }
}