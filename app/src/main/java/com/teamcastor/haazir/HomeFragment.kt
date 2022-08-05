package com.teamcastor.haazir

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.ncorti.slidetoact.SlideToActView
import com.teamcastor.haazir.data.model.LoginViewModel
import com.teamcastor.haazir.databinding.FragmentHomeBinding
import kotlin.system.exitProcess

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val loginViewModel: LoginViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentHomeBinding.bind(view)

        val onSlideCompleteListener = object : SlideToActView.OnSlideCompleteListener {
            override fun onSlideComplete(view: SlideToActView) {
                findNavController().navigate(R.id.action_HomeFragment_to_Attendance_Fragment)
            }
        }

        loginViewModel.getUser().observe(viewLifecycleOwner) { user ->
            user?.let {
                with(binding) {
                    name.text = user.name
                    employeeNumber.text = "Employee No: ${user.empNumber}"
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
                            LoginViewModel.logout()
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