package com.teamcastor.haazir

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.teamcastor.haazir.data.model.LoginViewModel
import com.teamcastor.haazir.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val loginViewModel: LoginViewModel by viewModels()
    private lateinit var navController: NavController
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var geofencingClient: GeofencingClient

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
    }

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.containsValue(false)) {
            initiateRequestPermissions(binding.root)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Needed for notifications
        createChannel(this)

        geofencingClient = LocationServices.getGeofencingClient(this)

        // Setup View binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Navigation
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

    override fun onStart() {
        super.onStart()
        if (checkPermissions()) {
            if (isLocationEnabled())
                addGeofence()
        } else {
            initiateRequestPermissions(binding.root)
        }
    }

    private fun addGeofence() {
        val geofence = Geofence.Builder()
            // Set the request ID of the geofence. This is a string to identify this
            // geofence.
            .setRequestId("niit-delhi")

            // Set the circular region of this geofence.
            .setCircularRegion(
                LATITUDE, LONGITUDE,  100F
            )
            // Set the transition types of interest. Alerts are only generated for these
            // transition. We track entry and exit transitions.
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            // Create the geofence.
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()

        fun getGeofencingRequest(): GeofencingRequest {
            return GeofencingRequest.Builder().apply {
                setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                addGeofence(geofence)
            }.build()
        }

        geofencingClient.addGeofences(getGeofencingRequest(), geofencePendingIntent).run {
            addOnSuccessListener {
                Log.i("MainActivity", "geofence added successfully")
            }
            addOnFailureListener {
                Log.w("MainActivity", "can't add geofence", it)
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun initiateRequestPermissions(view: View) {
        when {
            checkPermissions() -> {}
            REQUIRED_PERMISSIONS.any {
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )} -> {
                Utils.showSnackbar(
                    view,
                    "Missing Permissions",
                    Snackbar.LENGTH_INDEFINITE,
                    "Ok"
                ) {
                    requestPermissionsLauncher.launch(
                        REQUIRED_PERMISSIONS
                    )
                }
            }
            else -> requestPermissionsLauncher.launch(
                REQUIRED_PERMISSIONS
            )
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

    override fun onDestroy() {
        super.onDestroy()
//        removeGeofences()
    }

    private fun removeGeofences() {
        geofencingClient.removeGeofences(geofencePendingIntent)
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

    companion object {
        // Noida Institute of Technology
        const val LATITUDE = 28.463036
        const val LONGITUDE = 77.490994

        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
}
