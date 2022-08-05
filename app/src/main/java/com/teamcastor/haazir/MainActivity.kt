package com.teamcastor.haazir

import android.location.LocationManager
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.teamcastor.haazir.data.model.LoginViewModel
import com.teamcastor.haazir.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val loginViewModel: LoginViewModel by viewModels()
    private lateinit var navController: NavController
    private lateinit var bottomNavigationView: BottomNavigationView

    private lateinit var locationManager: LocationManager
    private val locationPermissionCode = 2
    private lateinit var str: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setupNavigation()

        bottomNavigationView = binding.bottomNavigation
        bottomNavigationView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.AttendanceFragment -> {
                    bottomNavigationView.visibility = View.GONE
                    binding.statusCard.visibility = View.VISIBLE

                }
                else -> {
                    bottomNavigationView.visibility = View.VISIBLE
                    binding.statusCard.visibility = View.GONE
                }
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        loginViewModel.authenticationState.observe(this) { authenticationState ->
            if (authenticationState != LoginViewModel.AuthenticationState.AUTHENTICATED) {
                    navController.navigate(R.id.LoginActivity)
            }
        }
    }

    private fun setupNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navHostFragment.navController
        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
        when (Firebase.auth.currentUser) {
            null -> {
                navGraph.setStartDestination(R.id.LoginActivity)
            }
            else -> {
                navGraph.setStartDestination(R.id.HomeFragment)
            }
        }
        navController.graph = navGraph
    }

}
