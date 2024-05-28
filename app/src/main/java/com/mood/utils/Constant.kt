package com.mood.utils

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.mood.BeanDailyApplication
import com.mood.R
import com.mood.data.database.BeanViewModelFactory

object Constant {
    const val BEAN_CALENDAR = "BEAN_CALENDAR"
    const val TAG = "doanvv"
    const val TYPE_JPEG = "image/jpeg"
    const val DATABASE_NAME = "bean_daily_database"
    const val IS_SET_UP_DATE = "is_set_up_date"
    const val IS_FIRST_OPEN = "is_first_open"
    const val TUTORIAL_STEP = "tutorial_step"
    const val TRIGGER_SOUND_SELECT = "trigger_sound_select"
    const val AMBIENT_SOUND_SELECT = "ambient_sound_select"
    const val TIME_COUNTDOWN: String = "time_countdown"
    const val IS_SHOW_TIME_SLEEP = "is_show_time_sleep"
    const val IS_SHOW_TODAY_PHOTO = "is_show_today_photo"
    const val IS_SHOW_TODAY_NOTE = "is_show_today_note"
    const val TYPE = "type"
    const val IS_ADD = "is_add"
    const val BEAN_EDIT = "bean_edit"
    const val BEAN_DAY = "bean_day"
    const val BEAN_MONTH = "bean_month"
    const val BEAN_YEAR = "bean_year"
    const val IMAGE_SHARE = "image_share"
    const val SORT_ASC = "ASC"
    const val SORT_DESC = "DESC"
    const val IS_SHOW_NOTIFICATION = "is_show_notification"
    const val IS_PREMIUM = "is_premium"
    const val IS_FROM_START_APP = "is_from_start_app"
    const val IS_FIRST_REQUEST_NOTY = "is_first_request_notification"

    //-------------------alarm interval day-----------------------
    const val MILLISECOND_ONE_DAY = 86400000
    const val AM9 = "9AM"
    const val PM3 = "3PM"
    const val PM7 = "7PM"
    private const val H9AM = 9
    private const val H3PM = 15
    private const val H7PM = 19
    val mapAlarmValue = mutableMapOf(
        AM9 to H9AM, PM3 to H3PM, PM7 to H7PM
    )
    val actions = listOf(AM9, PM3, PM7)

    //------------------------------------------------------------
    const val NOTI_DATA = "data"
    const val CHANNEL_NOTIFY_ID = "bean_notification"
    const val NOTI_1 = "NOTI_1"
    const val NOTI_2 = "NOTI_2"

    const val GET_PASSCODE = "getPassCode"
    const val IS_SHOW_PASSCODE = "is_show_passcode"
    const val IS_SHOW_FINGER_PRINT = "is_show_finger_print"
    const val IS_CUSTOM_BACKGROUND_IMAGE = "is_custom_background_image"
    const val BACKGROUND_IMAGE_APP = "background_image_app"
    const val IS_FULL_SCREEN_MODE = "is_full_screen_mode"
    const val IS_DARK_MODE = "is_dark_mode"

    var mapImageGallery = mutableMapOf<Uri, Bitmap>()

    fun getViewModelFactory(application: Application): BeanViewModelFactory {
        return BeanViewModelFactory((application as BeanDailyApplication).repository)
    }

    enum class WeekString(private val value: Int, val number: Int) {
        Monday(R.string.monday, 2),
        Tuesday(R.string.tuesday, 3),
        Wednesday(R.string.wednesday, 4),
        Thursday(R.string.thursday, 5),
        Friday(R.string.friday, 6),
        Saturday(R.string.saturday, 7),
        Sunday(R.string.sunday, 1);

        fun getTitle(context: Context, weekString: WeekString): String {
            return context.getString(weekString.value)
        }
    }

    fun getWeekTitleStartWithMonday(context: Context) = WeekString.values().map { it.getTitle(context, it) }.toMutableList()

    var orderIconByBlock: MutableList<Any> = mutableListOf()

    var isFilter: Boolean = false
    var iconFilter: Int = -10
    var sourceId: Int = 0

    //---------------------------- native ad ----------------------------
    const val NATIVE_AD_LOAD1 = ""
    const val NATIVE_AD_LOAD2 = ""
    const val NATIVE_AD_TEST = ""

    //-------------------------------------------------------------------

    var isFirstOpenSplash = false
    var isShowDialogNeedPermission = true
    var isShowDialogWatchAds = true
    var isShowDialogWatchAdsRelax = true
    var isPremium = MutableLiveData(SharePrefUtils.isBought())

    var isShowRateApp = true
    var isCountClickSave = MutableLiveData(0)
    const val COUNT_OPEN_APP = "count_open_app"
    const val IS_SHOW_DIALOG = "is_show_dialog"
    const val TIME_TO_REPEAT_RATE = "time_to_repeat_rate"
}