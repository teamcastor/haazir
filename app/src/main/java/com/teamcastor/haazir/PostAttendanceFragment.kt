package com.teamcastor.haazir

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.teamcastor.haazir.databinding.FragmentPostAttendanceBinding


class PostAttendanceFragment : Fragment(R.layout.fragment_post_attendance) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentPostAttendanceBinding.bind(view)
        binding.goBackButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

}