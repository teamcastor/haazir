package com.teamcastor.haazir

import android.content.Context
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.PagerAdapter

class LeaveAdapter(private val myContext: LeaveFragment, fm: FragmentManager, internal var totalTabs: Int) : FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        when (position) {
            0 -> {
                return ApplyLeaveFragment()
            }
            1 -> {
                return ApplyLeaveFragment()
            }
            2 -> {
                // val movieFragment = MovieFragment()
                return StatusLeaveFragment()
            }
            else -> return HomeFragment()
        }
    }

    override fun getCount(): Int {
        return totalTabs
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return true
    }

}
