package com.mood.utils

data class AlbumModel(
    val coverUri: String?,
    val name: String,
    var albumPhotos: MutableList<ImageModel>? = mutableListOf()
)
