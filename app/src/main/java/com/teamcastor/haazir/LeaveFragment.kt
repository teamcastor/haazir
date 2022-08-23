package com.teamcastor.haazir

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.teamcastor.haazir.databinding.FragmentLeaveBinding


class LeaveFragment : Fragment(R.layout.fragment_leave) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentLeaveBinding.bind(view)

        val pager = binding.leavePager
        val tabLayout = binding.leavetabLayout

        val fragmentList = listOf(
            Pair("Apply", ApplyLeaveFragment()),
            Pair("Status", StatusLeaveFragment())
        )

        val adapter = LeaveViewPagerAdapter(requireActivity() as AppCompatActivity, fragmentList)
        pager.adapter = adapter

        TabLayoutMediator(tabLayout, pager) { tab, position ->
            tab.text = fragmentList[position].first
        }.attach()
    }
}