package com.teamcastor.haazir

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.teamcastor.haazir.content.LeaveHistoryContent
import com.teamcastor.haazir.data.Leave
import com.teamcastor.haazir.data.model.AppViewModel
import com.teamcastor.haazir.databinding.FragmentStatusLeaveBinding

class StatusLeaveFragment : Fragment(R.layout.fragment_status_leave) {

    private val appViewModel: AppViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentStatusLeaveBinding.bind(view)

        fun updateListData(map: Map<String, Leave>?) {
            if (map != null) {
                for (k in map.keys) {
                    LeaveHistoryContent.addItem(k, map[k])
                }
                binding.list.adapter?.let { adapter ->
                    (adapter as LeaveHistoryRecyclerViewAdapter).updateData(
                        LeaveHistoryContent.ITEMS.sortedBy {
                            it.date
                        }
                    )
                }
            } else {
                LeaveHistoryContent.ITEM_MAP.clear()
            }
        }

        appViewModel.leaveHistory.observe(viewLifecycleOwner) { it1 ->
            updateListData(it1)
        }

        with(binding.list) {
            layoutManager = LinearLayoutManager(context)
            // Set the adapter
            adapter = LeaveHistoryRecyclerViewAdapter(LeaveHistoryContent.ITEMS)
        }
    }
}