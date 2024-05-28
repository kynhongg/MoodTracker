package com.mood.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

const val GRAND_PERMISSION = PackageManager.PERMISSION_GRANTED

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
const val READ_MEDIA_IMAGES = Manifest.permission.READ_MEDIA_IMAGES
const val READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE
const val WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
const val PERMISSION_POST_NOTIFICATION = Manifest.permission.POST_NOTIFICATIONS

fun checkPermission(context: Context, permission: String): Boolean {
    return ContextCompat.checkSelfPermission(context, permission) == GRAND_PERMISSION
}

fun Context.checkReadImagePermission(): Boolean {
    return if (isSdk33()) {
        checkPermission(READ_MEDIA_IMAGES)
    } else {
        checkPermission(READ_EXTERNAL_STORAGE)
    }
}

private fun AppCompatActivity.requestPermission(permission: String) {
    registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {

    }.launch(mutableListOf(permission).toTypedArray())
}

/**notification permission with android 13 and above*/
fun AppCompatActivity.requestNotifyPermission() {
    if (isSdk33() && !checkPermission(this, PERMISSION_POST_NOTIFICATION)) {
        requestPermission(PERMISSION_POST_NOTIFICATION)
    }
}