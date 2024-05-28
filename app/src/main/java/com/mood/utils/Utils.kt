package com.mood.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.os.Bundle
import java.io.Serializable

fun String.trackingEvent() {
}

fun Boolean.invert(): Boolean = !this

inline fun <reified T : Serializable> Bundle.getDataSerializable(key: String, clazz: Class<T>): T? {
    return if (isSdk33()) {
        getSerializable(key, clazz)
    } else {
        @Suppress("DEPRECATION") getSerializable(key) as? T
    }
}

fun overlayBitMap(bmp1: Bitmap, bmp2: Bitmap): Bitmap {
    val bmOverlay = Bitmap.createBitmap(bmp1.width, bmp1.height, bmp1.config)
    val bitmap2 = Bitmap.createScaledBitmap(bmp2, bmOverlay.width, bmOverlay.height, false)
    val canvas = Canvas(bmOverlay)
    canvas.drawBitmap(bmp1, Matrix(), null)
    canvas.drawBitmap(bitmap2, 0f, 0f, null)
    bmp1.recycle()
    bmp2.recycle()
    bitmap2.recycle()
    return bmOverlay
}