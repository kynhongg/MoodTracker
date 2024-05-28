package com.mood.screen.tutorial

enum class TutorialStep(val step: Int) {
    One(1),
    Two(2),
    Three(3);

    companion object {
        fun tabSize() = values().size
        fun getTabByPosition(position: Int) = values().getOrNull(position) ?: One
    }
}