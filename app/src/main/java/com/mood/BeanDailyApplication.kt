package com.mood

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.mood.data.database.BeanDatabase
import com.mood.data.database.BeanRepository
import com.mood.service.AlarmUtil
import com.mood.utils.Constant
import com.mood.utils.RemoteConfigUtil
import com.mood.utils.SharePrefUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class BeanDailyApplication : Application(), Application.ActivityLifecycleCallbacks, LifecycleObserver {
    private var currentActivity: Activity? = null

    companion object {
        public var CHECK_INTER_SHOW = false
        const val TAG = Constant.TAG
    }

    //    var allIcons: List<IconEntity> = mutableListOf()
//    var allBeans: List<BeanDailyEntity> = mutableListOf()
    private val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { BeanDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { BeanRepository(database.beanDao()) }

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)
        SharePrefUtils.init(this)
        RemoteConfigUtil.init()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        AlarmUtil.setIntervalAlarm(this, Constant.actions)
    }

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
    }

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }
}