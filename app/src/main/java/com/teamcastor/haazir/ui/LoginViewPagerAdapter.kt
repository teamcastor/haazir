package com.teamcastor.haazir

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.teamcastor.haazir.ui.login.LoginFragment
import com.teamcastor.haazir.ui.register.RegisterFragment

private const val NUM_TABS = 2

class ViewPagerAdapter(activity: AppCompatActivity) :
    FragmentStateAdapter(activity) {

    override fun getItemCount(): Int {
        return NUM_TABS
    }

    override fun createFragment(position: Int): Fragment {
        if (position == 0) {
            return LoginFragment()
        }
        return RegisterFragment()
    }
}