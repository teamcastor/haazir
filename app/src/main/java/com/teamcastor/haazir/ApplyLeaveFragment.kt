package com.teamcastor.haazir

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.datepicker.MaterialDatePicker
import com.teamcastor.haazir.data.Leave
import com.teamcastor.haazir.data.model.AppViewModel
import com.teamcastor.haazir.databinding.FragmentApplyLeaveBinding


class ApplyLeaveFragment : Fragment(R.layout.fragment_apply_leave) {

    private val appViewModel: AppViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var fromDate: Long? = null
        var tillDate: Long? = null
        var reason: String? = null
        var halfDayDate: Long? = null
        var type: String? = null

        val binding = FragmentApplyLeaveBinding.bind(view)
        val materialDateBuilder: MaterialDatePicker.Builder<Long> =
            MaterialDatePicker.Builder.datePicker()

        materialDateBuilder.setTitleText("SELECT A DATE")

        val fromDatePicker = materialDateBuilder.build()
        val tillDatePicker = materialDateBuilder.build()
        val halfDayDatePicker = materialDateBuilder.build()

        with(binding) {
            from.setOnClickListener {
                if (!fromDatePicker.isVisible) {
                    fromDatePicker.show(parentFragmentManager, "MATERIAL_DATE_PICKER")
                }
            }
            fromDatePicker.addOnPositiveButtonClickListener {
                fromdatetext.text = " " + fromDatePicker.headerText
                fromDate = it
            }
            till.setOnClickListener {
                if (!tillDatePicker.isVisible) {
                    tillDatePicker.show(parentFragmentManager, "MATERIAL_DATE_PICKER")
                }
            }
            tillDatePicker.addOnPositiveButtonClickListener {
                tilldatetext.text = " " + tillDatePicker.headerText
                tillDate = it
            }
            date.setOnClickListener {
                if (!halfDayDatePicker.isVisible) {
                    halfDayDatePicker.show(parentFragmentManager, "MATERIAL_DATE_PICKER")
                }
            }
            halfDayDatePicker.addOnPositiveButtonClickListener {
                datehalf.text = " " + halfDayDatePicker.headerText
                halfDayDate = it
            }

            radio.check(R.id.regular)

            halfDay.setOnClickListener {
                from.visibility = View.INVISIBLE
                till.visibility = View.INVISIBLE
                date.visibility = View.VISIBLE
                datehalf.text = " Pick Date  "
                fromDate = null
                tillDate = null

            }
            regular.setOnClickListener {
                from.visibility = View.VISIBLE
                till.visibility = View.VISIBLE
                date.visibility = View.INVISIBLE
                fromdatetext.text = " Start Date  "
                tilldatetext.text = " End Date  "
                halfDayDate = null

            }

            apply.setOnClickListener {

                // Deduce leave type by visibility of half-day leave card visibility
                type = if (!date.isVisible) {
                    "Regular"
                } else {
                    "Half Day"
                }
                // Leave type is half-day but date is not selected
                if (date.isVisible && halfDayDate == null) {
                    Toast.makeText(activity, "Select half day leave date!!", Toast.LENGTH_LONG)
                        .show()
                }
                // Leave type is regular but dates are not selected
                else if (from.isVisible && (fromDate == null || tillDate == null)) {
                    Toast.makeText(activity, "Select Dates !!", Toast.LENGTH_LONG).show()
                } else {
                    // Reason is empty
                    if (TextUtils.isEmpty(reasontext.text.toString())) {
                        Toast.makeText(activity, "Enter reason in brief", Toast.LENGTH_LONG).show()
                    }
                    // Reason is not empty
                    else if (!TextUtils.isEmpty(reasontext.text.toString())) {
                        reason = reasontext.text.toString()

                        type?.let { it1 -> Log.i("ApplyLeaveFragment", it1) }
                        fromDate?.let { it1 -> Log.i("ApplyLeaveFragment", it1.toString()) }
                        tillDate?.let { it1 -> Log.i("ApplyLeaveFragment", it1.toString()) }
                        halfDayDate?.let { it1 -> Log.i("ApplyLeaveFragment", it1.toString()) }
                        reason?.let { it1 -> Log.i("ApplyLeaveFragment", it1) }
                        val leave = Leave(
                            leaveType = type,
                            fromDate = fromDate,
                            toDate = tillDate,
                            halfDate = halfDayDate,
                            reason = reason,
                            status = "Awaiting"
                        )
                        appViewModel.applyLeave(leave)
                    }
                }
            }
        }
    }
}
