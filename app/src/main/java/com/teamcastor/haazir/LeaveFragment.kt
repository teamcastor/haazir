package com.teamcastor.haazir

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.SlideDistanceProvider


class LeaveFragment : Fragment() {
    var tabLayout:TabLayout? = null
    var frame: FrameLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_leave, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        tabLayout = view.findViewById(R.id.leavetabLayout)
        frame = view.findViewById(R.id.frame)
//        viewPager = view?.findViewById(R.id.viewPager)
//        tabLayout!!.addTab(tabLayout!!.newTab().setText("Apply"))
//        tabLayout!!.addTab(tabLayout!!.newTab().setText("Status"))
//        tabLayout!!.tabGravity = TabLayout.GRAVITY_FILL
//        val adapter = tabLayout?.tabCount?.let {
//            LeaveAdapter(this,(activity as FragmentActivity).supportFragmentManager,
//                it
//            )
//        }
//        val adapter = LeaveAdapter(this,(activity as FragmentActivity).supportFragmentManager, tabLayout!!.tabCount)
//        viewPager!!.adapter = adapter
//        viewPager!!.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
//        tabLayout!!.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
//            override fun onTabSelected(tab: TabLayout.Tab) {
//                viewPager?.currentItem = tab.position
//            }
//            override fun onTabUnselected(tab: TabLayout.Tab) {
//
//            }
//            override fun onTabReselected(tab: TabLayout.Tab) {
//
//            }
//        })
        if (savedInstanceState == null) {
            (activity as FragmentActivity).supportFragmentManager.beginTransaction().replace(R.id.frame, ApplyLeaveFragment()).commit()
        }
        tabLayout!!.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> (activity as FragmentActivity).supportFragmentManager.beginTransaction().replace(R.id.frame, ApplyLeaveFragment())
                        .commit()
                    else -> (activity as FragmentActivity).supportFragmentManager.beginTransaction()
                        .replace(R.id.frame,StatusLeaveFragment()).commit()
                }

            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        super.onViewCreated(view, savedInstanceState)
    }

}