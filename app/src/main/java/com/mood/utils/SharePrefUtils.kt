package com.mood.utils

import android.content.Context
import android.content.SharedPreferences
import com.mood.R

object SharePrefUtils {
    private const val PREF_NAME = "BeanDaily"
    lateinit var sharePref: SharedPreferences
    fun init(context: Context) {
        sharePref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun <T> saveKey(key: String, value: T) {
        when (value) {
            is String -> sharePref.edit().putString(key, value).apply()
            is Int -> sharePref.edit().putInt(key, value).apply()
            is Boolean -> sharePref.edit().putBoolean(key, value).apply()
            is Long -> sharePref.edit().putLong(key, value).apply()
            is Float -> sharePref.edit().putFloat(key, value).apply()
        }

    }

    fun getString(key: String): String {
        return sharePref.getString(key, "")?.trim() ?: ""
    }

    fun getInt(key: String): Int {
        return sharePref.getInt(key, 0)
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return sharePref.getBoolean(key, defaultValue)
    }

    fun getLong(key: String): Long {
        return sharePref.getLong(key, 0L)
    }

    fun getFloat(key: String): Float {
        return sharePref.getFloat(key, 0f)
    }

    fun getIsShowTimeSleep() = sharePref.getBoolean(Constant.IS_SHOW_TIME_SLEEP, true)
    fun getIsShowTodayPhoto() = sharePref.getBoolean(Constant.IS_SHOW_TODAY_PHOTO, true)
    fun getIsShowTodayNote() = sharePref.getBoolean(Constant.IS_SHOW_TODAY_NOTE, true)

    fun setIsShowNotification(isShow: Boolean) {
        saveKey(Constant.IS_SHOW_NOTIFICATION, isShow)
    }

    fun isShowNotification(): Boolean {
        return sharePref.getBoolean(Constant.IS_SHOW_NOTIFICATION, true)
    }

    fun setPassCode(data: String) {
        saveKey(Constant.GET_PASSCODE, data)
    }

    fun getPassCode() = getString(Constant.GET_PASSCODE)

    fun setIsShowPasscode(isShowPasscode: Boolean) =
        saveKey(Constant.IS_SHOW_PASSCODE, isShowPasscode)

    fun isShowPasscode() = getBoolean(Constant.IS_SHOW_PASSCODE)

    fun setIsShowFingerprint(isShowFingerPrint: Boolean) =
        saveKey(Constant.IS_SHOW_FINGER_PRINT, isShowFingerPrint)

    fun isShowFingerprint() = getBoolean(Constant.IS_SHOW_FINGER_PRINT)

    fun isCustomBackgroundImage() = getBoolean(Constant.IS_CUSTOM_BACKGROUND_IMAGE)
    fun setIsCustomBackgroundImage(isCustomBackgroundImage: Boolean) =
        saveKey(Constant.IS_CUSTOM_BACKGROUND_IMAGE, isCustomBackgroundImage)

    fun getBackgroundImageApp(): Int {
        return sharePref.getInt(Constant.BACKGROUND_IMAGE_APP, R.drawable.background_app_1)
    }

    fun setBackgroundImageApp(backgroundResource: Int) {
        saveKey(Constant.BACKGROUND_IMAGE_APP, backgroundResource)
    }

    fun isFullScreenMode(): Boolean {
        return getBoolean(Constant.IS_FULL_SCREEN_MODE)
    }

    fun setIsFullScreenMode(isFullScreen: Boolean) =
        saveKey(Constant.IS_FULL_SCREEN_MODE, isFullScreen)

    fun isDarkMode(): Boolean {
        return getBoolean(Constant.IS_DARK_MODE)
    }

    fun setIsDarkMode(isDark: Boolean) = saveKey(Constant.IS_DARK_MODE, isDark)

    fun isBought() = true;
    fun setBought(isBought: Boolean) = saveKey(Constant.IS_PREMIUM, isBought)

    fun setFirstRequestNotification(data: Boolean) = saveKey(Constant.IS_FIRST_REQUEST_NOTY, data)

    fun isFirstRequestNotification() = getBoolean(Constant.IS_FIRST_REQUEST_NOTY, true)
}