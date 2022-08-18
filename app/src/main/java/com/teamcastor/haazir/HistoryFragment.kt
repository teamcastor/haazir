package com.teamcastor.haazir

import android.os.Bundle
import android.os.SystemClock
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.teamcastor.haazir.FirebaseRepository.Companion.month
import com.teamcastor.haazir.FirebaseRepository.Companion.today
import com.teamcastor.haazir.content.AttendanceHistoryContent
import com.teamcastor.haazir.data.Attendance
import com.teamcastor.haazir.data.model.AppViewModel
import com.teamcastor.haazir.databinding.FragmentHistoryListBinding
import java.util.*


/**
 * A fragment representing a list of Items.
 */
class HistoryFragment : Fragment(R.layout.fragment_history_list) {
    private var columnCount = 1
    private val appViewModel: AppViewModel by activityViewModels()
    private var startDate = MaterialDatePicker.thisMonthInUtcMilliseconds()
    private var endDate = MaterialDatePicker.todayInUtcMilliseconds()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bindingHL = FragmentHistoryListBinding.bind(view)

        fun updateListData(map: Map<String, Attendance>?) {
            if (map != null) {
                val filteredMap = map.filterKeys {
                    it.toLong() in startDate..endDate
                }
                println("filtered map: $filteredMap")
                for (k in filteredMap.keys) {
                    AttendanceHistoryContent.addItem(k, map[k])
                }
                bindingHL.list.adapter?.let { adapter ->
                    (adapter as AttendanceHistoryRecyclerViewAdapter).updateData(
                        AttendanceHistoryContent.ITEMS.sortedBy {
                            it.date
                        }
                    )
                }
            }
            AttendanceHistoryContent.ITEM_MAP.clear()
        }

        appViewModel.attendanceHistory.observe(viewLifecycleOwner) { it1 ->
            updateListData(it1)
        }

        appViewModel.historyRange.observe(viewLifecycleOwner) {
            var start = it.first.toDate() + " " + it.first.toMonth()
            var end = it.second.toDate() + " " + it.second.toMonth()
            if (it.first.toYear() != today.toYear() || it.second.toYear() != today.toYear()) {
                start += " ${it.first.toYear()}"
                end += " ${it.second.toYear()}"
            }
            bindingHL.range.text = start + "  —  " + end
            startDate = it.first
            endDate = it.second
            updateListData(appViewModel.attendanceHistory.value)
        }


        // Set adapter
        with(bindingHL.list) {
            // Add divider
            addItemDecoration(
                DividerItemDecoration(
                    context,
                    LinearLayoutManager.VERTICAL
                )
            )
            // Decide layout depending on number of column
            layoutManager = when {
                columnCount <= 1 -> LinearLayoutManager(context)
                else -> GridLayoutManager(context, columnCount)
            }
            // Set the adapter
            adapter = AttendanceHistoryRecyclerViewAdapter(AttendanceHistoryContent.ITEMS)
        }

        // Start is year 2010
        val constraints =
            CalendarConstraints.Builder()
                .setEnd(month)
                .setStart(1262304000000)
                .setValidator(DateValidatorPointBackward.now()).build()


        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setSelection(
                androidx.core.util.Pair(
                    MaterialDatePicker.thisMonthInUtcMilliseconds(),
                    MaterialDatePicker.todayInUtcMilliseconds()
                )
            )
            .setCalendarConstraints(constraints)


            .setTitleText("Select attendance date range")
            .build()

        bindingHL.datePicker.setOnClickListener {
            // Sometimes on consecutive presses, without this error comes:
            // ` java.lang.IllegalStateException: Fragment already added: MaterialDatePicker`
            if (!picker.isVisible) {
                picker.show(parentFragmentManager, "historyFragment")
            }
            picker.addOnPositiveButtonClickListener {
                it?.let {
                    appViewModel.historyRangeChanged(it)
                }
            }
        }
    }

}