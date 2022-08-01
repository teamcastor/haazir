package com.teamcastor.haazir

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.ScaleProvider
import com.google.android.material.transition.SlideDistanceProvider
import com.teamcastor.haazir.databinding.FragmentHistoryBinding
import com.teamcastor.haazir.databinding.FragmentHistoryListBinding
import com.teamcastor.haazir.placeholder.PlaceholderContent


/**
 * A fragment representing a list of Items.
 */
class HistoryFragment : Fragment() {
    private var _bindingH: FragmentHistoryBinding? = null
    private val bindingH get() = _bindingH!!
    private var _bindingHL: FragmentHistoryListBinding? = null
    private val bindingHL get() = _bindingHL!!
    private var columnCount = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _bindingHL = FragmentHistoryListBinding.inflate(inflater, container, false)
        // Set the adapte
        bindingHL.list.addItemDecoration(
            DividerItemDecoration(
                context,
                LinearLayoutManager.VERTICAL
            )
        )
        with(bindingHL.list) {
            layoutManager = when {
                columnCount <= 1 -> LinearLayoutManager(context)
                else -> GridLayoutManager(context, columnCount)
            }
            adapter = MyAttendanceDetailRecyclerViewAdapter(PlaceholderContent.ITEMS)
        }

        return bindingHL.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _bindingHL = null
    }

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            HistoryFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}