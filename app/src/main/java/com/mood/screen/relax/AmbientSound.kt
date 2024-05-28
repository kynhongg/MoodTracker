package com.mood.screen.relax

import com.mood.R

enum class AmbientSound(val soundNameId: Int, val imageSource: Int, val url: String) {
    City(R.string.city, R.drawable.ambient_sound_1, "city.mp3"),
    Universe(R.string.universe, R.drawable.ambient_sound_2, "universe.mp3"),
    ForestWind(R.string.forest_wind, R.drawable.ambient_sound_3, "forest_wind.mp3"),
    Tide(R.string.tide, R.drawable.ambient_sound_4, "tide.mp3"),
    Forest(R.string.forest, R.drawable.ambient_sound_5, "forest.mp3"),
    ThunderStorm(R.string.thunder_storm, R.drawable.ambient_sound_6, "thunderstorm.mp3"),
    SwayingLeaf(R.string.swaying_leaf, R.drawable.ambient_sound_9, "swaying_leaves.mp3"),
    Raining(R.string.raining, R.drawable.ambient_sound_7, "raining.mp3"),
    Mute(R.string.mute, R.drawable.mute, "")
}