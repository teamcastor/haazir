package com.teamcastor.haazir

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialFade
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.SlideDistanceProvider
import com.ncorti.slidetoact.SlideToActView
import com.teamcastor.haazir.data.model.LoginViewModel
import com.teamcastor.haazir.databinding.FragmentHomeBinding
import kotlin.system.exitProcess

class HomeFragment : Fragment() {
    companion object {
        const val LOGIN_SUCCESSFUL: String = "LOGIN_SUCCESSFUL"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    private var _binding: FragmentHomeBinding? = null
    private val loginViewModel: LoginViewModel by activityViewModels()
    private lateinit var savedStateHandle: SavedStateHandle

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val onSlideCompleteListener = object : SlideToActView.OnSlideCompleteListener {
            override fun onSlideComplete(view: SlideToActView) {
                findNavController().navigate(R.id.action_HomeFragment_to_Attendance_Fragment)
            }
        }
        binding.slider.onSlideCompleteListener = onSlideCompleteListener
        binding.topbar.toolbar.apply {
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
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}