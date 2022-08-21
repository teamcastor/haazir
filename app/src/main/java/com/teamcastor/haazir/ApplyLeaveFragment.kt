package com.teamcastor.haazir

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.*
import android.text.InputFilter.LengthFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.card.MaterialCardView
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.datepicker.MaterialDatePicker
import com.teamcastor.haazir.data.Leave
import com.teamcastor.haazir.data.model.AppViewModel
import com.teamcastor.haazir.databinding.FragmentApplyLeaveBinding
import java.util.*


class ApplyLeaveFragment : Fragment() {


    private lateinit var leave: Leave
    var half: Button? = null
    var regular: Button? = null
    var fromdate: TextView? = null
    var date: MaterialCardView? = null
    var datehalf: TextView? = null
    var from: MaterialCardView? = null
    var till: MaterialCardView? = null
    var tilldate: TextView? = null
    var reasontext: EditText? = null
    var to: TextView? = null
    var apply: Button? = null
    private val sharedPrefFile = "sharedpreference"
    private var count = 1
    private var temp = 1

    private val appViewModel: AppViewModel by activityViewModels()
    private var _binding: FragmentApplyLeaveBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?
    ): View? {
        var leave_type = ""
        var leave_numofdays = ""
        var from_date = ""
        var till_date = ""
        var date_halfday = ""
        var reason_text = ""
        val materialDateBuilder: MaterialDatePicker.Builder<*> =
            MaterialDatePicker.Builder.datePicker()
        materialDateBuilder.setTitleText("SELECT A DATE")
        val materialDatePicker = materialDateBuilder.build()
        val materialDatePicker1 = materialDateBuilder.build()
        val materialDatePicker2 = materialDateBuilder.build()

//        val sharedPreferences: SharedPreferences =
//            requireActivity().getSharedPreferences(sharedPrefFile,Context.MODE_PRIVATE)
//        val editor:SharedPreferences.Editor =  sharedPreferences.edit()


        _binding = FragmentApplyLeaveBinding.inflate(inflater,container,false)
        date = binding.date
        datehalf = binding.datehalf
        from = binding.from
        fromdate = binding.fromdatetext
        till = binding.till
        tilldate = binding.tilldatetext
        half = binding.halfDay
        regular = binding.regular
        reasontext = binding.reasontext
        apply = binding.apply
        from!!.setOnClickListener{
            materialDatePicker.show(parentFragmentManager, "MATERIAL_DATE_PICKER")
        }
        materialDatePicker.addOnPositiveButtonClickListener {
            fromdate?.text = " " + materialDatePicker.headerText
        }
        till!!.setOnClickListener{
            materialDatePicker1.show(parentFragmentManager, "MATERIAL_DATE_PICKER")
        }
        materialDatePicker1.addOnPositiveButtonClickListener {
            tilldate?.text = " " + materialDatePicker1.headerText
        }
        date!!.setOnClickListener{
            materialDatePicker2.show(parentFragmentManager, "MATERIAL_DATE_PICKER")
        }
        materialDatePicker2.addOnPositiveButtonClickListener {
            datehalf?.text = " " + materialDatePicker2.headerText
        }

//        reasontext!!.filters = arrayOf<InputFilter>(LengthFilter(30))
//
//
        half!!.setOnClickListener{
            temp = 1
            from?.visibility = View.INVISIBLE
            till?.visibility =  View.INVISIBLE
            date?.visibility = View.VISIBLE
            datehalf!!.text = " Pick Date  "
            fromdate?.text = null
            tilldate?.text = null
        }
        regular!!.setOnClickListener(){
            temp = 2
            from?.visibility = View.VISIBLE
            till?.visibility =  View.VISIBLE
            date?.visibility = View.INVISIBLE
            fromdate?.text = " Start Date  "
            tilldate?.text = " End Date  "
            datehalf!!.text = null

        }

        apply!!.setOnClickListener(){
            val i=0
            while (i!=1){
                if(temp == 2){
                    leave_type = "Regular"
                }
                else if (temp == 1){
                    leave_type = "Half Day"
                }
                else if(temp == 0){
                    Toast.makeText(activity,"Select Leave Type !!",Toast.LENGTH_LONG).show()
                    break
                }
                if(temp == 2 && fromdate!!.text == " Start Date  "){
                    Toast.makeText(activity,"Select Date !!",Toast.LENGTH_LONG).show()
                    break
                }
                else if(temp == 2 && fromdate!!.text != " Start Date  "){
                    from_date = fromdate!!.text.toString()
                    date_halfday = ""
                }
                if(temp == 2 && tilldate!!.text == " End Date    "){
                    Toast.makeText(activity,"Select Dates !!",Toast.LENGTH_LONG).show()
                    break
                }
                else if(temp == 2 && tilldate!!.text != " End Date    "){
                    till_date =  tilldate!!.text.toString()
                    date_halfday = ""
                }
                if(temp == 1 && datehalf!!.text == " Pick Date  "){
                    Toast.makeText(activity,"Select Date !!",Toast.LENGTH_LONG).show()
                    break
                }
                else if(temp == 1 && datehalf!!.text != " Pick Date  "){
                    date_halfday = datehalf!!.text.toString()
                    from_date = ""
                    till_date = ""
                }
                if(TextUtils.isEmpty(reasontext!!.text.toString())){
                    Toast.makeText(activity,"Enter reason in brief",Toast.LENGTH_LONG).show()
                    break
                }
                else if (!TextUtils.isEmpty(reasontext!!.text.toString())){
                    reason_text = reasontext!!.text.toString()

                    Log.i("ApplyLeaveFragment", leave_type)
                    Log.i("ApplyLeaveFragment", from_date)
                    Log.i("ApplyLeaveFragment", till_date)
                    Log.i("ApplyLeaveFragment", date_halfday)
                    Log.i("ApplyLeaveFragment", reason_text)
                    leave = Leave(
                        leaveType = leave_type,
                        fromDate = from_date,
                        toDate = till_date,
                        halfDate = date_halfday,
                        reason = reason_text,
                    )
                    appViewModel.applyLeave(leave,count)
                    count++;
//                    editor.putInt("count",count)
//                    editor.apply()
                    break
                }
            }

        }
        return binding.root
    }
//    inner class MinMaxFilter() : InputFilter {
//        private var intMin: Int = 0
//        private var intMax: Int = 0
//
//        // Initialized
//        constructor(minValue: Int, maxValue: Int) : this() {
//            this.intMin = minValue
//            this.intMax = maxValue
//        }
//
//        override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dStart: Int, dEnd: Int): CharSequence? {
//            try {
//                val input = Integer.parseInt(dest.toString() + source.toString())
//                if (isInRange(intMin, intMax, input)) {
//                    return null
//                }
//            } catch (e: NumberFormatException) {
//                e.printStackTrace()
//            }
//            return ""
//        }
//        private fun isInRange(a: Int, b: Int, c: Int): Boolean {
//            return if (b > a) c in a..b else c in b..a
//        }
//    }

}