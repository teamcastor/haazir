package com.teamcastor.haazir

import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.navigation.NavController
import android.widget.TextView
import androidx.navigation.findNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.android.gms.location.*
import com.teamcastor.haazir.data.model.LoginViewModel
import com.teamcastor.haazir.databinding.ActivityMainBinding
import kotlin.system.exitProcess
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.SystemClock.sleep
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.*
import com.google.android.material.tabs.TabLayoutMediator


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val loginViewModel: LoginViewModel by viewModels()
    private lateinit var navController: NavController
    private lateinit var bottomNavigationView: BottomNavigationView

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private val permissionId = 2

    private lateinit var str: String



//    private lateinit var tv: TextView
//    private lateinit var tv1: TextView
//    private lateinit var btn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setTitleTextAppearance(this, R.style.SmarkanTextAppearance)

        setupNavigation()

        bottomNavigationView = binding.bottomNavigation
        bottomNavigationView.setupWithNavController(navController)



    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.viewPagerFragment -> {
                    bottomNavigationView.visibility = View.GONE
                    binding.toolbar.visibility = View.GONE
                }
                R.id.AttendanceFragment -> {
//                    bottomNavigationView.visibility = View.GONE
                    bottomNavigationView.visibility = View.GONE
                    binding.statusCard.visibility = View.VISIBLE

                }
                else -> {
                    binding.toolbar.title = null
                    bottomNavigationView.visibility = View.VISIBLE
                    binding.toolbar.visibility = View.VISIBLE
                    binding.statusCard.visibility = View.GONE
                }
            }
        }
        // I don't know what is up with these, they keep throwing NullPointerException
        // Since they don't work anyway, I'm just gonna comment them out.
        // Others can fix it properly
//        tv = findViewById(R.id.textViewlat)
//        tv1 = findViewById(R.id.textViewlon)
//        btn = findViewById(R.id.btnLocation)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//        btn.setOnClickListener {
//            locRequest()
//            getLocation()
//        }

        loginViewModel.authenticationState.observeForever { authenticationState ->
            when (authenticationState) {
                LoginViewModel.AuthenticationState.AUTHENTICATED -> {
                    navController.navigate(R.id.action_global_HomeFragment)
                }
                else -> {
                    navController.navigate(R.id.action_global_ViewPagerFragment)
                }
            }
            // Need to update start destination
            setupNavigation()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_tool_bar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_logout -> {
                LoginViewModel.logout()
                true
            }
            R.id.action_exit -> {
                exitProcess(0)
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navHostFragment.navController
        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
        when (Firebase.auth.currentUser) {
            null -> {
                navGraph.setStartDestination(R.id.viewPagerFragment)
            }
            else -> {
                navGraph.setStartDestination(R.id.HomeFragment)
            }
        }
        navController.graph = navGraph
    }

    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result
                    if (location != null) {
                        val geocoder = Geocoder(this, Locale.getDefault())
                        val list: List<Address> =
                            geocoder.getFromLocation(location.latitude, location.longitude, 1)
//                            tv.text = "Latitude\n${list[0].latitude}"
//                            tv1.text = "Longitude\n${list[0].longitude}"
//                            tvLongitude.text = "Longitude\n${list[0].latitude}"
//                            tvLongitude.text = "Longitude\n${list[0].longitude}"
//                            tvCountryName.text = "Country Name\n${list[0].countryName}"
//                            tvLocality.text = "Locality\n${list[0].locality}"
//                            tvAddress.text = "Address\n${list[0].getAddressLine(0)}"
                    }
                }
            } else {
                Toast.makeText(this, "Please turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            permissionId
        )
    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == permissionId) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLocation()
            }
        }
    }

    private fun locRequest() {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener { locationSettingsResponse ->
            val state = locationSettingsResponse .locationSettingsStates

            val label = "GPS >> (Present: ${state.isGpsPresent}  | Usable: ${state.isGpsUsable} ) \n\n" +
                    "Network >> ( Present: ${state.isNetworkLocationPresent} | Usable: ${state.isNetworkLocationUsable} ) \n\n" +
                    "Location >> ( Present: ${state.isLocationPresent} | Usable: ${state.isLocationUsable} )"

            Toast.makeText(this, label, Toast.LENGTH_SHORT).show()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(
                        this,
                        100
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    println(sendEx.message)
                }
            }
        }
    }
}
