package com.mood.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mood.data.entity.BeanDailyEntity
import com.mood.data.entity.BeanIconEntity
import com.mood.data.entity.BeanImageAttachEntity
import com.mood.data.entity.BlockEmojiEntity
import com.mood.data.entity.IconEntity
import com.mood.data.entity.MusicCalmEntity
import com.mood.utils.Constant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(
    entities = [
        BeanDailyEntity::class,
        IconEntity::class,
        BlockEmojiEntity::class,
        BeanIconEntity::class,
        BeanImageAttachEntity::class,
        MusicCalmEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class BeanDatabase : RoomDatabase() {

    abstract fun beanDao(): BeanDao

    companion object {
        private var INSTANCE: BeanDatabase? = null
        fun getDatabase(context: Context, scope: CoroutineScope): BeanDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BeanDatabase::class.java,
                    Constant.DATABASE_NAME
                )
                    .addCallback(WordDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }

    private class WordDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    populateDatabase(database.beanDao())
                }
            }
        }

        suspend fun populateDatabase(beanDao: BeanDao) {
            beanDao.deleteAllBlock()
            beanDao.deleteAllIcons()
            beanDao.deleteAllBeans()
            beanDao.deleteAllBeanIcons()
            beanDao.deleteAllBeanImageAttach()
            initDatabase(beanDao)
        }

        private suspend fun initDatabase(beanDao: BeanDao) {
            insertDefaultBlock(beanDao)
            insertDefaultIcon(beanDao)
//            insertDefaultBean(beanDao)
//            insertDefaultBeanIcon(beanDao)
        }

        private suspend fun insertDefaultBlock(beanDao: BeanDao) {
            val listBlock = mutableListOf<BlockEmojiEntity>()
            val blockName = listOf(
                "Funny", "Weather", "Social", "Meal",
                "Hobbies", "Health", "Other", "Romance",
                "School", "Work", "Chores", "Beauty", "Events"
            )
            blockName.forEachIndexed { index, name ->
                listBlock.add(BlockEmojiEntity().apply {
                    this.blockName = name
                    blockIsShow = true
                    blockOrder = index + 1
                })
            }
            listBlock.forEach { beanDao.insertBlock(it) }
        }

        private suspend fun insertDefaultBeanIcon(beanDao: BeanDao) {
            val listBeanIcon = mutableListOf(
                BeanIconEntity().apply {
                    beanIconId = 1
                    iconId = 1
                },
                BeanIconEntity().apply {
                    beanIconId = 1
                    iconId = 2
                },
                BeanIconEntity().apply {
                    beanIconId = 1
                    iconId = 3
                },
                BeanIconEntity().apply {
                    beanIconId = 1
                    iconId = 4
                },
                BeanIconEntity().apply {
                    beanIconId = 1
                    iconId = 5
                },
                BeanIconEntity().apply {
                    beanIconId = 2
                    iconId = 1
                },
                BeanIconEntity().apply {
                    beanIconId = 2
                    iconId = 3
                },
                BeanIconEntity().apply {
                    beanIconId = 3
                    iconId = 3
                },
                BeanIconEntity().apply {
                    beanIconId = 3
                    iconId = 4
                },
                BeanIconEntity().apply {
                    beanIconId = 3
                    iconId = 5
                },
            )
            listBeanIcon.forEach { beanDao.insertBeanIcons(it) }
        }

        private suspend fun insertDefaultBean(beanDao: BeanDao) {
            val listBean = mutableListOf(
                BeanDailyEntity().apply {
                    beanTypeId = 1
                    beanIconId = 1
                    beanDescription = "hello i'm Doan, 123 456 789, lorem abc xyz"
                    year = 2023
                    month = 5
                    day = 10
                    hour = 10
                    minutes = 20
                    timeGoToBed = "22:40"
                    timeWakeup = "07:15"
                },
                BeanDailyEntity().apply {
                    beanTypeId = 2
                    beanIconId = 2
                    beanDescription = "hello i'm supper man, 123 456 789, lorem abc xyz"
                    year = 2023
                    month = 5
                    day = 11
                    hour = 10
                    minutes = 20
                    timeGoToBed = "23:30"
                    timeWakeup = "08:05"
                },
                BeanDailyEntity().apply {
                    beanTypeId = 3
                    beanIconId = 3
                    beanDescription = "hello i'm zootube, i never stupid"
                    year = 2023
                    month = 5
                    day = 9
                    hour = 10
                    minutes = 21
                    timeGoToBed = "00:30"
                    timeWakeup = "06:25"
                },
                BeanDailyEntity().apply {
                    beanTypeId = 4
                    beanIconId = 4
                    beanDescription = "hello i'm iron man"
                    year = 2023
                    month = 5
                    day = 8
                    hour = 9
                    minutes = 28
                    timeGoToBed = "21:10"
                    timeWakeup = "05:45"
                },
                BeanDailyEntity().apply {
                    beanTypeId = 5
                    beanIconId = 5
                    beanDescription = ""
                    year = 2023
                    month = 5
                    day = 7
                    hour = 6
                    minutes = 7
                    timeGoToBed = "23:20"
                    timeWakeup = "08:05"
                },
                BeanDailyEntity().apply {
                    beanTypeId = 6
                    beanIconId = 6
                    beanDescription = ""
                    year = 2023
                    month = 5
                    day = 6
                    hour = 14
                    minutes = 35
                    timeGoToBed = "03:20"
                    timeWakeup = "09:25"
                },
            )
            listBean.forEach { beanDao.insertBeans(it) }
        }

        private suspend fun insertDefaultIcon(beanDao: BeanDao) {
            val iconName1 = listOf(
                "Excited", "Relaxed", "Proud", "Hopeful", "Happy", "Enthusiastic", "Butterflies",
                "Refreshed", "Gloomy", "Lonely", "Anxious", "Sad", "Angry", "Burdensome", "Annoyed", "Tired"
            )
            val iconName2 = listOf("Sunny", "Cloudy", "Rainy", "Snowy")
            val iconName3 = listOf("Friend", "Family", "S/o", "Acquaintance", "None")
            val iconName4 = listOf("Breakfast", "Lunch", "Dinner", "Night snack")
            val iconName5 = listOf(
                "Exercise", "Move & TV", "Gaming", "Reading", "Instrument playing", "Taking a walk",
                "Listening to music", "Painting"
            )
            val iconName6 = listOf("Flu", "Hospitalization", "Clinic", "Medicine")
            val iconName7 = listOf("Alcohol", "Smoking", "Coffee", "Snack", "Beverage")
            val iconName8 = listOf("Date", "Anniversary", "Gift", "Conflict", "Sex")
            val iconName9 = listOf("Class", "Study", "Homework", "Exam", "Group project")
            val iconName10 = listOf("End on time", "Overtime", "Staff meal", "Business trip")
            val iconName11 = listOf("Cleaning", "Cooking", "Laundry", "Dishes")
            val iconName12 = listOf("Haircut", "Manicure", "Skincare", "Makeup")
            val iconName13 = listOf("Cinema", "Theme park", "Shopping", "Picnic", "Stay home", "Party", "Restaurant", "Travel")
            val mapIconBlock = mapOf(
                1 to iconName1, 2 to iconName2, 3 to iconName3, 4 to iconName4,
                5 to iconName5, 6 to iconName6, 7 to iconName7, 8 to iconName8,
                9 to iconName9, 10 to iconName10, 11 to iconName11, 12 to iconName12, 13 to iconName13
            )
            val list = mutableListOf<IconEntity>()
            mapIconBlock.keys.forEach { blockId ->
                val iconNames = mapIconBlock[blockId] ?: mutableListOf()
                iconNames.forEach { name ->
                    list.add(
                        IconEntity(
                            iconBlockId = blockId, iconName = name, iconUrl = "R.drawable.ic_icon_" + list.size,
                            iconIsShow = true, iconOrder = list.size + 1
                        )
                    )
                }
            }
            list.forEach {
                beanDao.insertIcon(it)
            }
        }
    }
}