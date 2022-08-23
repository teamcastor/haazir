package com.teamcastor.haazir

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class LeaveViewPagerAdapter(
    activity: AppCompatActivity,
    private val fragmentList: List<Pair<String, Fragment>>
) :
    FragmentStateAdapter(activity) {

    override fun getItemCount(): Int {
        return fragmentList.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position].second
    }
}