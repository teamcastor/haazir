package com.teamcastor.haazir

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.ncorti.slidetoact.SlideToActView
import com.teamcastor.haazir.data.model.AppViewModel
import com.teamcastor.haazir.databinding.FragmentHomeBinding
import kotlin.system.exitProcess

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val appViewModel: AppViewModel by activityViewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentHomeBinding.bind(view)

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
        appViewModel.sliderFormState.observe(viewLifecycleOwner) {
            with(binding) {
                if (it.event == "in")
                    slider.text = getString(R.string.slider_check_in)
                else
                    slider.text = getString(R.string.slider_check_out)
                slider.isLocked = it.isLocked
                slider.visibility = it.Visibility
                slider.isReversed = it.isReversed
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
}
