package com.teamcastor.haazir

import android.content.IntentSender
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.ncorti.slidetoact.SlideToActView
import com.teamcastor.haazir.data.model.AppViewModel
import com.teamcastor.haazir.databinding.FragmentHomeBinding
import kotlin.system.exitProcess

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val appViewModel: AppViewModel by activityViewModels()

    private val REQUEST_CHECK_SETTINGS = 0x1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentHomeBinding.bind(view)

        createLocationRequest()

        val onSlideCompleteListener = object : SlideToActView.OnSlideCompleteListener {
            override fun onSlideComplete(view: SlideToActView) {
                findNavController().navigate(R.id.action_HomeFragment_to_Attendance_Fragment)

            }
        }

        appViewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                with(binding) {
                    name.text = it.name
                    employeeNumber.text = "Employee No: ${it.empNumber}"
                }
            }
        }

        appViewModel.attendanceHistory.observe(viewLifecycleOwner) {
            println("Attendance history: $it")
        }
        appViewModel.sliderFormState.observe(viewLifecycleOwner) {
            with(binding) {
                if (it.event == "in")
                    slider.text = getString(R.string.slider_check_in)

                else
                    slider.text = getString(R.string.slider_check_out)
                if (it.isLocked)
                    slider.text = "Slider locked outside geofence area"
                slider.isLocked = it.isLocked
                slider.visibility = it.Visibility
                slider.isReversed = it.isReversed
            }
        }

        AppViewModel.geofenceState.observe(viewLifecycleOwner) {
            if (it == null || it == false) {
                binding.location.text = "Outside geofence area"
                binding.slider.isLocked = true
                appViewModel.sliderStateChanged()
            } else {
                binding.location.text = "Inside geofence area"
                appViewModel.sliderStateChanged()
            }
        }

        appViewModel.attendanceToday.observe(viewLifecycleOwner) { attendance ->
            appViewModel.sliderStateChanged(attendance)
            with(binding) {
                workDuration.text = "--:--"
                enterTime.text = attendance?.checkIn?.toTimeIST() ?: "--:--"
                exitTime.text = attendance?.checkOut?.toTimeIST() ?: "--:--"
                if (attendance?.checkIn != null && attendance.checkOut != null) {
                    workDuration.text =
                        (attendance.checkOut!! - attendance.checkIn!!).toDurationHM()
                }
            }
        }

        with(binding) {
            slider.onSlideCompleteListener = onSlideCompleteListener

            topbar.toolbar.apply {
                inflateMenu(R.menu.menu_tool_bar)
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.action_logout -> {
                            AppViewModel.logout()
                            true
                        }
                        R.id.action_exit -> {
                            exitProcess(0)
                        }
                        else -> false
                    }
                }
            }
        }
    }

    fun createLocationRequest() {

        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = Priority.PRIORITY_BALANCED_POWER_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client: SettingsClient = LocationServices.getSettingsClient(requireActivity())
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener { locationSettingsResponse ->
            // ..
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                // ..
                try {
                    // ..
                    exception.startResolutionForResult(requireActivity(), REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }
}
