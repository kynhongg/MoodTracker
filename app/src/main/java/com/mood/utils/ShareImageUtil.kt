package com.mood.utils

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.provider.MediaStore
import java.io.IOException

object ShareImageUtil {
    private fun sdkCheck() = isSdkQ()

    @SuppressLint("InlinedApi")
    fun savePhotoToExternalStorage(contentResolver: ContentResolver, name: String, bmp: Bitmap?): Boolean {
        val imageCollection: Uri = if (sdkCheck()) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$name.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, Constant.TYPE_JPEG)
            if (bmp != null) {
                put(MediaStore.Images.Media.WIDTH, bmp.width)
                put(MediaStore.Images.Media.HEIGHT, bmp.height)
            }
        }

        return try {
            contentResolver.insert(imageCollection, contentValues)?.also {
                contentResolver.openOutputStream(it).use { outputStream ->
                    outputStream?.let {
                        bmp?.let {
                            if (!bmp.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)) {
                                throw IOException("Failed to save Bitmap")
                            }
                        }
                    }
                }
            } ?: throw IOException("Failed to create Media Store entry")
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    fun mergeBitmapsVertical(bitmap1: Bitmap, bitmap2: Bitmap): Bitmap {
        val width = bitmap1.width.coerceAtLeast(bitmap2.width)
        val height = bitmap1.height + bitmap2.height

        val mergedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(mergedBitmap)

        val zeroFloat = 0f
        canvas.drawBitmap(bitmap1, zeroFloat, zeroFloat, null)
        canvas.drawBitmap(bitmap2, zeroFloat, bitmap1.height.toFloat(), null)

        return mergedBitmap
    }
}