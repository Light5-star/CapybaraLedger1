package com.xuhh.capybaraledger.ui.activity.reminder

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ReminderPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ReminderListFragment()
            1 -> ReminderAddFragment()
            else -> throw IllegalArgumentException("Invalid position $position")
        }
    }
} 