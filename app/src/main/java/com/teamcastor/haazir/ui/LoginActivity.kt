package com.teamcastor.haazir.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.teamcastor.haazir.MainActivity
import com.teamcastor.haazir.ViewPagerAdapter
import com.teamcastor.haazir.data.model.AppViewModel
import com.teamcastor.haazir.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewPager: ViewPager2
    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val fragmentArray = arrayOf(
            "Login",
            "Register"
        )
        viewPager = binding.pager
        val tabLayout = binding.tabLayout

        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = fragmentArray[position]
        }.attach()

        appViewModel.authenticationState.observe(this) { authenticationState ->
            if (authenticationState == AppViewModel.AuthenticationState.AUTHENTICATED) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }

    }
    override fun onBackPressed() {
        if (viewPager.currentItem == 0) {
            // If the user is currently looking at the first fragment, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed()
        } else {
            // Otherwise, select the previous previous fragment.
            viewPager.currentItem = viewPager.currentItem - 1
        }
    }
}