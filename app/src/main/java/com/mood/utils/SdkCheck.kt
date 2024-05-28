package com.mood.utils

import android.os.Build

/** SDK 23 - M*/
fun isSdkM() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

/** SDK 26 - O*/
fun isSdkO() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

/** SDK 29 - Q*/
fun isSdkQ() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

/** SDK 30 - R*/
fun isSdkR() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

/** SDK 33 - TIRAMISU*/
fun isSdk33() = Build.VERSION.SDK_INT >= 33