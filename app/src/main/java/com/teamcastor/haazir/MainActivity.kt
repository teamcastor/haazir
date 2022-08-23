package com.teamcastor.haazir

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.teamcastor.haazir.data.model.AppViewModel
import com.teamcastor.haazir.databinding.ActivityMainBinding
import com.teamcastor.haazir.ui.LoginActivity
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val appViewModel: AppViewModel by viewModels()
    private lateinit var navController: NavController
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.containsValue(false)) {
            initiateRequestPermissions(binding.root)
        }
        // Call it again for requesting background location permission
        if (!checkBLPermission())
            initiateRequestPermissions(binding.root)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

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
                    binding.bottomBarPlaceholder.visibility = View.VISIBLE

                }
                R.id.PostAttendanceFragment -> {
                    bottomNavigationView.visibility = View.GONE
                }
                else -> {
                    bottomNavigationView.visibility = View.VISIBLE
                    binding.bottomBarPlaceholder.visibility = View.GONE
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()
        initiateRequestPermissions(binding.root)
        if (checkPermissions() && checkBLPermission()) {
            if (isLocationEnabled())
                getLocation()
            addGeofence()
        }
    }

    @RequiresApi(29)
    private fun showBackLocDialog() {
        val label = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            packageManager.backgroundPermissionOptionLabel
        } else {
            "Allow all the time"
        }
        MaterialAlertDialogBuilder(this)
            .setTitle("Allow background location access")
            .setMessage("Select '${label}' on the upcoming screen.")
            .setCancelable(false)
            .setNegativeButton("Quit Application") { dialog, which ->
                // Respond to negative button press
                exitProcess(0)
            }
            .setPositiveButton("Proceed") { dialog, which ->
                requestPermissionsLauncher.launch(
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                )
            }
            .show()
    }

    private fun mockDetected() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Warning")
            .setMessage("Mock location detected.")
            .setPositiveButton("Ok") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    @Suppress("DEPRECATION")
    @SuppressLint("MissingPermission")
    private fun getLocation() {
        // PRIORITY_HIGH_ACCURACY takes upto 15s to return location, so not using that
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                        if (location.isFromMockProvider) {
                            if (!this.isFinishing) {
                                mockDetected()
                            }
                        }
                    } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (location.isMock) {
                            if (!this.isFinishing) {
                                mockDetected()
                            }
                        }
                    }
                    val lat = location.latitude
                    val lon = location.longitude
                    println("Latitude: $lat and Longitude: $lon")
                }
                // The location can be null, if this starts happening too often we will have to
                // implement https://developer.android.com/training/location/request-updates#updates
                Log.w("getLocation()", "Null location was returned by fLClient")
            }
            .addOnFailureListener {
                Log.w("getLocation()", "Failed to get location", it)
                Toast.makeText(this, "Cannot get location.", Toast.LENGTH_SHORT).show()
            }
    }

    @SuppressLint("MissingPermission")
    private fun addGeofence() {
        val geofence = Geofence.Builder()
            // Set the request ID of the geofence. This is a string to identify this
            // geofence. Something to note is that system won't add a geofence if
            // a previous one with same Id exists, even if lat and long changes.
            .setRequestId("jmit")

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

    private fun checkBLPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
                    == PackageManager.PERMISSION_GRANTED)
        } else
            true
    }
    private fun checkPermissions() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun initiateRequestPermissions(view: View) {
        when {
            checkPermissions() -> {
                if (!checkBLPermission())
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        showBackLocDialog()
                    }
            }
            REQUIRED_PERMISSIONS.any {
                ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    it
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

        appViewModel.authenticationState.observe(this) { authenticationState ->
            if (authenticationState != AppViewModel.AuthenticationState.AUTHENTICATED) {
                navController.navigate(R.id.LoginActivity)
                finish()
            }
        }
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
//        const val LATITUDE = 28.463036
//        const val LONGITUDE = 77.490994
        // JMIT
        const val LATITUDE = 30.036659738247568
        const val LONGITUDE = 77.16009847819805

        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
}
