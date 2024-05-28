package com.mood.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mood.R
import com.mood.utils.Constant

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null || intent.action == null) {
            return
        }
        when (intent.action) {
            Constant.AM9 -> {
                createNotification(
                    context,
                    listOf(
                        context?.resources?.getString(R.string.notification_content_1) ?: "",
                        context?.resources?.getString(R.string.notification_content_2) ?: ""
                    ),
                    Constant.NOTI_1
                )
            }

            Constant.PM3 -> {
                createNotification(
                    context,
                    listOf(
                        context?.resources?.getString(R.string.notification_content_3) ?: "",
                        context?.resources?.getString(R.string.notification_content_4) ?: ""
                    ),
                    Constant.NOTI_2
                )
            }

            Constant.PM7 -> {
                createNotification(
                    context,
                    listOf(
                        context?.resources?.getString(R.string.notification_content_5) ?: "",
                        context?.resources?.getString(R.string.notification_content_6) ?: ""
                    ),
                    Constant.NOTI_1
                )
            }
        }
    }

    private fun createNotification(context: Context?, listNotificationContent: List<String>, type: String) {
        val content = getContentRandom(listNotificationContent.toMutableList())
        TodayNotification.showNotify(
            context,
            context?.resources?.getString(R.string.app_name),
            content,
            type
        )
    }

    private fun getContentRandom(list: MutableList<String>): String {
        for (i in 0..5) {
            list.shuffle()
        }
        return list[0]
    }
}