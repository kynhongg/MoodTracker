package com.mood.utils

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

class TrackingUtils {
    companion object {
        private lateinit var mFirebaseAnalytics: FirebaseAnalytics
        fun trackingEventScreen(eventLog: String) {
            try {
                mFirebaseAnalytics = Firebase.analytics
                val params = Bundle()
                params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, eventLog)
                mFirebaseAnalytics.logEvent(eventLog, params)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}