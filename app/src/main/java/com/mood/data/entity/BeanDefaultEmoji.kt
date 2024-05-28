package com.mood.data.entity

import android.content.Context
import com.mood.R

enum class BeanDefaultEmoji(val icon: Int, val status: Int, val background: Int, val iconOff: Int) {
    Default(R.drawable.ic_bean_type_default, 0, 0, R.drawable.ic_bean_type_default),
    Type1(R.drawable.ic_bean_type_1, R.string.status_1, R.drawable.bg_bean_type_1, R.drawable.ic_bean_type_1_off),
    Type2(R.drawable.ic_bean_type_2, R.string.status_2, R.drawable.bg_bean_type_2, R.drawable.ic_bean_type_2_off),
    Type3(R.drawable.ic_bean_type_3, R.string.status_3, R.drawable.bg_bean_type_3, R.drawable.ic_bean_type_3_off),
    Type4(R.drawable.ic_bean_type_4, R.string.status_4, R.drawable.bg_bean_type_4, R.drawable.ic_bean_type_4_off),
    Type5(R.drawable.ic_bean_type_5, R.string.status_5, R.drawable.bg_bean_type_5, R.drawable.ic_bean_type_5_off),
    Type6(R.drawable.ic_bean_type_6, R.string.status_6, R.drawable.bg_bean_type_6, R.drawable.ic_bean_type_6_off),
    Type7(R.drawable.ic_bean_type_7, R.string.status_7, R.drawable.bg_bean_type_7, R.drawable.ic_bean_type_7_off),
    Type8(R.drawable.ic_bean_type_8, R.string.status_8, R.drawable.bg_bean_type_8, R.drawable.ic_bean_type_8_off);

    companion object {

        fun getStatusByIndex(context: Context, index: Int) = if (index >= values().size) {
            context.getString(Default.status)
        } else {
            context.getString(values()[index].status)
        }

        fun getImageIdByIndex(index: Int): Int {
            return if (index >= values().size) Default.icon
            else values()[index].icon
        }

        fun getBackgroundResourceByIndex(index: Int): Int {
            return if (index >= values().size) {
                Default.background
            } else {
                values()[index].background
            }
        }
    }
}