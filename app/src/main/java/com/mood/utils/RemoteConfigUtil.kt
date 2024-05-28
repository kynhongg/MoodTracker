package com.mood.utils

import com.mood.R
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings

object RemoteConfigUtil {
    private const val IS_SHOW_BANNER = "show_banner"
    private const val IS_SHOW_INTER = "show_inter"
    private const val IS_SHOW_NATIVE = "show_native"
    private const val IS_SHOW_OPEN_AD = "show_open_ad"

    @JvmStatic
    fun init() {
        val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 45
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        remoteConfig.fetchAndActivate()
    }

    private fun getBooleanValue(key: String): Boolean {
        return FirebaseRemoteConfig.getInstance().getBoolean(key)
    }

    @JvmStatic
    val isShowBannerAd: Boolean
//        get() = getBooleanValue(IS_SHOW_BANNER)
        get() = false

    @JvmStatic
    val isShowInter: Boolean
//        get() = getBooleanValue(IS_SHOW_INTER)
        get() = false

    @JvmStatic
    val isShowNative: Boolean
//        get() = getBooleanValue(IS_SHOW_NATIVE)
        get() = false


    @JvmStatic
    val isShowOpenAd: Boolean
//        get() = getBooleanValue(IS_SHOW_OPEN_AD)
        get() = false

}