package com.mood.service

import android.app.AlarmManager
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.mood.R
import com.mood.utils.Constant
import com.mood.utils.SharePrefUtils
import com.mood.utils.isSdkM
import com.mood.utils.isSdkO
import java.util.Calendar

object AlarmUtil {
    fun setIntervalAlarm(context: Context, actions: List<String>) {
        createNotificationChannel(context)
        actions.forEach { action ->
            setAlarm(context, action)
        }
    }

    private fun createNotificationChannel(context: Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (isSdkO()) {
            val name: CharSequence = context.getString(R.string.channel_name)
            val description = context.getString(R.string.channel_desc)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(Constant.CHANNEL_NOTIFY_ID, name, importance)
            channel.description = description
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun setAlarm(context: Context, action: String) {
        cancelAlarms(context, action)
        val alarmIntentToday = Intent(context, AlarmReceiver::class.java)
        alarmIntentToday.action = action
        val flag: Int = getFlagNotification()
        val pendingIntentToday = PendingIntent.getBroadcast(context, 0, alarmIntentToday, flag)
        val managerToday = context.getSystemService(Application.ALARM_SERVICE) as AlarmManager
        val todayCal = Calendar.getInstance()
        todayCal.apply {
            set(Calendar.HOUR_OF_DAY, Constant.mapAlarmValue[action] ?: 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (timeInMillis < System.currentTimeMillis()) {
                timeInMillis += Constant.MILLISECOND_ONE_DAY
            }
        }
        if (SharePrefUtils.isShowNotification()) {
            managerToday.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                todayCal.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntentToday
            )
        }
    }

    private fun cancelAlarms(context: Context, action: String) {
        val alarmIntentToday = Intent(context.applicationContext, AlarmReceiver::class.java)
        alarmIntentToday.action = action
        val flag: Int = getFlagNotification(PendingIntent.FLAG_NO_CREATE)
        val pendingIntentToday = PendingIntent.getBroadcast(
            context.applicationContext,
            0,
            alarmIntentToday,
            flag
        )
        val alarmManager = context.getSystemService(Application.ALARM_SERVICE) as AlarmManager
        if (pendingIntentToday != null) {
            alarmManager.cancel(pendingIntentToday)
        }
    }

    private fun getFlagNotification(flagWant: Int = PendingIntent.FLAG_UPDATE_CURRENT) = if (isSdkM()) {
        flagWant or PendingIntent.FLAG_IMMUTABLE
    } else {
        flagWant
    }
}