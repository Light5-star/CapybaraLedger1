package com.xuhh.capybaraledger.ui.fragment.detail

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class DetailPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    private var calendarFragment: CalendarModeFragment? = null

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FlowModeFragment()
            1 -> CalendarModeFragment().also { calendarFragment = it }
            else -> throw IllegalArgumentException("Invalid position $position")
        }
    }

    fun getCalendarFragment(): CalendarModeFragment? = calendarFragment
} 