package com.mood.data.database

import androidx.annotation.WorkerThread
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import com.mood.data.entity.BeanDailyEntity
import com.mood.data.entity.BeanIconEntity
import com.mood.data.entity.BeanImageAttachEntity
import com.mood.data.entity.BlockEmojiEntity
import com.mood.data.entity.IconEntity
import com.mood.data.entity.MusicCalmEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class BeanRepository(private val beanDao: BeanDao) {
    val allBean: Flow<List<BeanDailyEntity>> = beanDao.getAllBeansFlow()
    val allBlock: Flow<List<BlockEmojiEntity>> = beanDao.getAllBlockFlow()
    val allIcons: Flow<List<IconEntity>> = beanDao.getAllIcon()
    val allBeanIcon: Flow<List<BeanIconEntity>> = beanDao.getAllBeanIconsFlow()
    val allBeanImageAttachEntity: Flow<List<BeanImageAttachEntity>> = beanDao.getAllBeanImageAttachFlow()

    suspend fun <T> getResult(
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        request: suspend CoroutineScope.() -> T
    ): Result<T> {
        return withContext(dispatcher) {
            val x = MutableLiveData(10)
            x.switchMap { x }
            try {
                Result.success(request())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    //beans
    @WorkerThread
    suspend fun insertBean(beanDailyEntity: BeanDailyEntity): Long {
        return beanDao.insertBeans(beanDailyEntity)
    }

    suspend fun updateBean(beanDailyEntity: BeanDailyEntity) {
        beanDao.updateBean(beanDailyEntity)
    }

    @WorkerThread
    suspend fun deleteBean(beanId: Int) {
        beanDao.deleteBeans(beanId)
    }

    suspend fun getAllBeans(): List<BeanDailyEntity> {
        return withContext(Dispatchers.IO) {
            beanDao.getAllBeans()
        }
    }

    suspend fun getMaxBeanIconId(): Long {
        return withContext(Dispatchers.IO) {
            beanDao.getMaxBeanIconId()
        }
    }

    //icons
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertIcon(iconEntity: IconEntity) {
        beanDao.insertIcon(iconEntity)
    }

    suspend fun deleteIconByBlockId(blockId: Int) {
        beanDao.deleteIconByBlockId(blockId)
    }

    @WorkerThread
    suspend fun deleteAllIcon() {
        beanDao.deleteAllIcons()
    }

    suspend fun getAllIcon(): List<IconEntity> {
        return withContext(Dispatchers.IO) {
            beanDao.getAllIcon2()
        }
    }

    suspend fun updateIcon(iconEntity: IconEntity) {
        beanDao.updateIcon(iconEntity)
    }

    suspend fun deleteIcon(iconId: Int) {
        beanDao.deleteIcon(iconId)
    }

    // block
    suspend fun getAllBlock(): List<BlockEmojiEntity> {
        return withContext(Dispatchers.IO) {
            beanDao.getAllBlock()
        }
    }

    suspend fun insertBlock(blockEmojiEntity: BlockEmojiEntity) {
        beanDao.insertBlock(blockEmojiEntity)
    }

    suspend fun deleteBlock(blockId: Int) {
        beanDao.deleteBlock(blockId)
    }

    suspend fun updateBlock(blockEmojiEntity: BlockEmojiEntity) {
        beanDao.updateBlock(blockEmojiEntity)
    }

    //bean_icons
    suspend fun insertBeanIcon(beanIconEntity: BeanIconEntity) {
        beanDao.insertBeanIcons(beanIconEntity)
    }

    suspend fun getAllBeanIconById(id: Int): List<BeanIconEntity> {
        return withContext(Dispatchers.IO) {
            beanDao.getAllBeanIconById(id)
        }
    }

    suspend fun getAllBeanIcon(): List<BeanIconEntity> {
        return withContext(Dispatchers.IO) {
            beanDao.getAllBeanIcons()
        }
    }

    suspend fun deleteBeanIconByIconId(iconId: Int) {
        beanDao.deleteBeanIconByIconId(iconId)
    }

    suspend fun deleteBeanIconByBeanIconId(beanIconId: Int) {
        beanDao.deleteBeanIconByBeanIconId(beanIconId)
    }

    suspend fun deleteBeanIconByIconAndBean(iconId: Int, beanIconId: Int) {
        beanDao.deleteBeanIconByIconAndBean(iconId, beanIconId)
    }

    //bean attach image
    suspend fun getAllBeanImageAttach(beanId: Int): List<BeanImageAttachEntity> {
        return withContext(Dispatchers.IO) {
            beanDao.getAllBeanImageAttachById(beanId)
        }
    }

    suspend fun getAllBeanImageAttach(): List<BeanImageAttachEntity> {
        return withContext(Dispatchers.IO) {
            beanDao.getAllBeanImageAttach()
        }
    }

    suspend fun insertBeanImageAttach(beanImageAttachEntity: BeanImageAttachEntity) {
        beanDao.insertBeanImageAttach(beanImageAttachEntity)
    }

    suspend fun deleteBeanImageAttachById(beanId: Int): Int {
        return withContext(Dispatchers.IO) {
            beanDao.deleteAllBeanImageAttachWithBeanId(beanId)
        }
    }

    //music calm
    suspend fun getAllMusicCalm(): List<MusicCalmEntity> {
        return withContext(Dispatchers.IO) {
            beanDao.getAllMusicCalms()
        }
    }

    suspend fun getSecondCalmWithMonth(month: Int, year: Int): Int {
        return withContext(Dispatchers.IO) {
            beanDao.getSecondMusicCalmsWithMonth(month, year)
        }
    }

    suspend fun insertMusicCalm(musicCalmEntity: MusicCalmEntity) {
        beanDao.insertMusicCalm(musicCalmEntity)
    }

    suspend fun updateSecondCalmWithMonthYear(second: Int, month: Int, year: Int) {
        beanDao.updateMusicCalm(second, month, year)
    }

    suspend fun updateMusicCalm(musicCalmEntity: MusicCalmEntity) {
        beanDao.updateMusicCalm(musicCalmEntity)
    }
}