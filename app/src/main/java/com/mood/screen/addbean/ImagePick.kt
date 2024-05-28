package com.mood.screen.addbean

import android.net.Uri

data class ImagePick(
    val uri: Uri,
    var isSelected: Boolean = false
)
