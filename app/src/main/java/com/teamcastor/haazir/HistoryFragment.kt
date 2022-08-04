package com.teamcastor.haazir

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import by.dzmitry_lakisau.month_year_picker_dialog.MonthYearPickerDialog
import com.teamcastor.haazir.databinding.FragmentHistoryListBinding
import com.teamcastor.haazir.placeholder.PlaceholderContent


/**
 * A fragment representing a list of Items.
 */
class HistoryFragment : Fragment(R.layout.fragment_history_list) {
    private var columnCount = 1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bindingHL = FragmentHistoryListBinding.bind(view)
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
            adapter = MyAttendanceDetailRecyclerViewAdapter(PlaceholderContent.ITEMS)
        }

        val picker = MonthYearPickerDialog.Builder(
            context = requireContext(),
            themeResId = R.style.Style_MonthYearPickerDialog,
            onDateSetListener = { year, month ->
                // Add one because it starts counting from zero (really?)
                var twoDigitMonth = (month + 1).toString()
                if (twoDigitMonth.toInt() < 10) {
                    twoDigitMonth = ("0$twoDigitMonth")
                }
                bindingHL.datePicker.setText("${twoDigitMonth}/$year")
            },
            selectedMonth = 7,
            selectedYear = 2022
        ).build()

        bindingHL.datePickerLayout.setEndIconOnClickListener {
            picker.show()
        }
        picker.setTitle("Select Month and Year")
    }

}