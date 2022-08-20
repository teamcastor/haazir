package com.teamcastor.haazir.ui

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.teamcastor.haazir.ui.login.LoginFragment
import com.teamcastor.haazir.ui.register.RegisterFragment


class ViewPagerAdapter(activity: AppCompatActivity, private val fragmentList: MutableList<Pair<String, Fragment>>) :
    FragmentStateAdapter(activity) {

    override fun getItemCount(): Int {
        return fragmentList.size
    }
    // Without overriding getItemId and containsItem, fragment will simply
    // be reused instead of calling createFragment
    override fun getItemId(position: Int): Long {
        return if (fragmentList[position].second is LoginFragment
            || fragmentList[position].second is RegisterFragment) {
            super.getItemId(position)
        } else {
            fragmentList[position].second.hashCode().toLong()
        }
    }

    override fun containsItem(itemId: Long): Boolean {
        return if (itemId.toInt() in 0 .. 1) {
            super.containsItem(itemId)
        } else {
            fragmentList.find { it.second.hashCode().toLong() == itemId } != null
        }
    }


    fun replaceRegisterFragment(gotVector: Boolean) {
        if (gotVector) {
            fragmentList.removeAt(1)
            fragmentList.add(1, Pair("Register", RegisterFragment()))
        }
        notifyItemChanged(1)
    }

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position].second
    }
}