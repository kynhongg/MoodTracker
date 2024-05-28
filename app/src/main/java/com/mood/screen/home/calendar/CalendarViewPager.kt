package com.mood.screen.home.calendar

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mood.utils.Constant

class CalendarViewPager(
    private var listCalendarBean: List<CalendarBean>,
    fragmentManager: FragmentManager, lifeCycle: Lifecycle
) : FragmentStateAdapter(
    fragmentManager, lifeCycle
) {
    override fun getItemCount(): Int {
        return listCalendarBean.size
    }

    override fun createFragment(position: Int): Fragment {
        val bundle = Bundle().apply {
            putSerializable(Constant.BEAN_CALENDAR, listCalendarBean[position])
        }
        return CalendarFragmentV2().apply {
            arguments = bundle
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()

    }
}