package com.teamcastor.haazir.ui

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.activity.addCallback
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.teamcastor.haazir.R
import com.teamcastor.haazir.ViewPagerAdapter
import com.teamcastor.haazir.databinding.FragmentViewPagerBinding

class ViewPagerFragment : Fragment() {
    private var _binding: FragmentViewPagerBinding? = null
    private var viewPager: ViewPager2? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentViewPagerBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fragmentArray = arrayOf(
            "Login",
            "Register"
        )
        viewPager = binding.pager
        val tabLayout = binding.tabLayout

        val adapter = ViewPagerAdapter(parentFragmentManager, lifecycle)
        viewPager!!.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager!!) { tab, position ->
            tab.text = fragmentArray[position]
        }.attach()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (viewPager!!.currentItem == 0) {
                // Firstly remove this callback, since requireActivity().onBackPressed() will call this callback
                // otherwise, leading to infinite loop.
                this.remove()
                // If the user is currently looking at the first fragment, allow the system to handle the
                // Back button. This calls finish() on this activity and pops the back stack.
                requireActivity().onBackPressed()
            } else {
                // Otherwise, select the previous previous fragment.
                viewPager!!.currentItem = viewPager!!.currentItem - 1
            }

        }

    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}