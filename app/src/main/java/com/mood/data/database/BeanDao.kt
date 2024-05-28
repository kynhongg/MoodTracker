package com.mood.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mood.data.entity.BeanDailyEntity
import com.mood.data.entity.BeanIconEntity
import com.mood.data.entity.BeanImageAttachEntity
import com.mood.data.entity.BlockEmojiEntity
import com.mood.data.entity.IconEntity
import com.mood.data.entity.MusicCalmEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BeanDao {

    //beans

    @Query("select * from beans")
    fun getAllBeans(): List<BeanDailyEntity>

    @Query("select * from beans")
    fun getAllBeansFlow(): Flow<List<BeanDailyEntity>>

    @Query("select max(bean_icon_id) from beans")
    fun getMaxBeanIconId(): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBeans(beanDailyEntity: BeanDailyEntity): Long

    @Update
    suspend fun updateBean(beanDailyEntity: BeanDailyEntity)

    @Query("delete from beans where beanId = (:beanId)")
    suspend fun deleteBeans(beanId: Int)

    @Query("DELETE FROM beans")
    suspend fun deleteAllBeans()

    ///icons
    @Query("select * from icons")
    fun getAllIcon(): Flow<List<IconEntity>>

    @Query("select * from icons")
    fun getAllIcon2(): List<IconEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIcon(iconEntity: IconEntity): Long

    @Update
    suspend fun updateIcon(iconEntity: IconEntity)

    @Query("DELETE FROM icons WHERE iconId = (:iconId)")
    suspend fun deleteIcon(iconId: Int)

    @Query("DELETE FROM icons")
    suspend fun deleteAllIcons()

    @Query("DELETE FROM icons WHERE icon_block_id = (:blockId)")
    suspend fun deleteIconByBlockId(blockId: Int)

    //bean_icons
    @Query("select * from bean_icons")
    fun getAllBeanIcons(): List<BeanIconEntity>

    @Query("select * from bean_icons")
    fun getAllBeanIconsFlow(): Flow<List<BeanIconEntity>>

    @Query("select * from bean_icons where beanIconId =(:id)")
    fun getAllBeanIconById(id: Int): List<BeanIconEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBeanIcons(beanIconEntity: BeanIconEntity): Long

    @Query("DELETE FROM bean_icons")
    suspend fun deleteAllBeanIcons()

    @Query("DELETE FROM bean_icons WHERE iconId =(:iconId)")
    suspend fun deleteBeanIconByIconId(iconId: Int)

    @Query("DELETE FROM bean_icons WHERE beanIconId =(:beanIconId)")
    suspend fun deleteBeanIconByBeanIconId(beanIconId: Int)

    @Query("DELETE FROM bean_icons WHERE iconId = (:iconId) AND beanIconId = (:beanIconId)")
    suspend fun deleteBeanIconByIconAndBean(iconId: Int, beanIconId: Int)

    //bean_attach
    @Query("select * from bean_image_attach where beanId =:beanId")
    fun getAllBeanImageAttachById(beanId: Int): List<BeanImageAttachEntity>

    @Query("select * from bean_image_attach")
    fun getAllBeanImageAttach(): List<BeanImageAttachEntity>

    @Query("select * from bean_image_attach")
    fun getAllBeanImageAttachFlow(): Flow<List<BeanImageAttachEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBeanImageAttach(beanImageAttachEntity: BeanImageAttachEntity): Long

    @Query("DELETE FROM bean_image_attach")
    suspend fun deleteAllBeanImageAttach()

    @Query("DELETE FROM bean_image_attach WHERE beanId =(:beanId)")
    suspend fun deleteAllBeanImageAttachWithBeanId(beanId: Int): Int

    //block
    @Query("select * from blocks")
    fun getAllBlockFlow(): Flow<List<BlockEmojiEntity>>

    @Query("select * from blocks")
    fun getAllBlock(): List<BlockEmojiEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBlock(blockEmojiEntity: BlockEmojiEntity): Long

    @Query("DELETE FROM blocks")
    suspend fun deleteAllBlock()

    @Query("DELETE FROM blocks WHERE blockId = (:blockId)")
    suspend fun deleteBlock(blockId: Int)

    @Update
    suspend fun updateBlock(blockEmojiEntity: BlockEmojiEntity)

    //calm
    @Query("select * from music_calms")
    fun getAllMusicCalms(): List<MusicCalmEntity>

    @Query("select calm_second from music_calms where calm_month =(:month) and calm_year = (:year)")
    fun getSecondMusicCalmsWithMonth(month: Int, year: Int): Int

    @Insert
    suspend fun insertMusicCalm(musicCalmEntity: MusicCalmEntity)

    @Update
    suspend fun updateMusicCalm(musicCalmEntity: MusicCalmEntity)

    @Query("update music_calms set calm_second=(:second) where calm_month =(:month) and calm_year = (:year)")
    suspend fun updateMusicCalm(second: Int, month: Int, year: Int)
}