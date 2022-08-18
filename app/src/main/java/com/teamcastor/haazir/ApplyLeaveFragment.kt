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
import com.google.android.material.datepicker.MaterialDatePicker
import com.teamcastor.haazir.data.Leave
import com.teamcastor.haazir.data.model.AppViewModel
import com.teamcastor.haazir.databinding.FragmentApplyLeaveBinding
import java.util.*


class ApplyLeaveFragment : Fragment() {


    private lateinit var leave: Leave
    var half: RadioButton? = null
    var regular: RadioButton? = null
    var numofdays: LinearLayout? = null
    var numofdaysedit: EditText? = null
    var fromdate: TextView? = null
    var date: LinearLayout? = null
    var datehalf: TextView? = null
    var from: LinearLayout? = null
    var fromtext: TextView? = null
    var till: LinearLayout? = null
    var tilldate: TextView? = null
    var reasontext: EditText? = null
    var apply: Button? = null
    private val sharedPrefFile = "sharedpreference"
    private var count = 1

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
        from = binding.fromdate
        fromdate = binding.fromdatetext
        till = binding.tilldate
        tilldate = binding.tilldatetext
        numofdays = binding.numofdays
        numofdaysedit = binding.numofdaysedit
        fromtext = binding.from
        half = binding.halfDay
        regular = binding.regular
        reasontext = binding.reasontext
        apply = binding.apply

        date?.visibility = View.INVISIBLE

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
        numofdaysedit!!.filters = arrayOf<InputFilter>(MinMaxFilter(1, 7))
        reasontext!!.filters = arrayOf<InputFilter>(LengthFilter(30))


        half!!.setOnClickListener{

            numofdays?.visibility = View.INVISIBLE
            from?.visibility = View.INVISIBLE
            till?.visibility =  View.INVISIBLE
            date?.visibility = View.VISIBLE
            datehalf!!.text = " Pick Date  "
            fromdate?.text = null
            tilldate?.text = null
            numofdaysedit?.text = null
            fromtext!!.text = "Date"
        }
        regular!!.setOnClickListener(){
            numofdays?.visibility = View.VISIBLE
            from?.visibility = View.VISIBLE
            till?.visibility =  View.VISIBLE
            date?.visibility = View.INVISIBLE
            fromdate?.text = " Pick Date  "
            tilldate?.text = " Pick Date  "
            datehalf!!.text = null
            fromtext!!.text = "From"

        }

        apply!!.setOnClickListener(){
            val i=0
            while (i!=1){
                if(regular!!.isChecked){
                    leave_type = "Regular"
                }
                else if (half!!.isChecked){
                    leave_type = "Half Day"
                }
                else if(!regular!!.isChecked && !half!!.isChecked){
                    Toast.makeText(activity,"Select Leave Type !!",Toast.LENGTH_LONG).show()
                    break
                }
                if(regular!!.isChecked && TextUtils.isEmpty(numofdaysedit!!.text.toString())){
                    Toast.makeText(activity,"Enter number of days.",Toast.LENGTH_LONG).show()
                    break
                }
                else if(regular!!.isChecked && !TextUtils.isEmpty(numofdaysedit!!.text.toString())){
                    leave_numofdays = numofdaysedit!!.text.toString()
                }
                if(regular!!.isChecked && fromdate!!.text == " Pick Date  "){
                    Toast.makeText(activity,"Select Date !!",Toast.LENGTH_LONG).show()
                    break
                }
                else if(regular!!.isChecked && fromdate!!.text != " Pick Date  "){
                    from_date = fromdate!!.text.toString()
                    date_halfday = ""
                }
                if(regular!!.isChecked && tilldate!!.text == " Pick Date  "){
                    Toast.makeText(activity,"Select Dates !!",Toast.LENGTH_LONG).show()
                    break
                }
                else if(regular!!.isChecked && tilldate!!.text != " Pick Date  "){
                    till_date =  tilldate!!.text.toString()
                    date_halfday = ""
                }
                if(half!!.isChecked && datehalf!!.text == " Pick Date  "){
                    Toast.makeText(activity,"Select Date !!",Toast.LENGTH_LONG).show()
                    break
                }
                else if(half!!.isChecked && datehalf!!.text != " Pick Date  "){
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
                    Log.i("ApplyLeaveFragment", leave_numofdays)
                    Log.i("ApplyLeaveFragment", from_date)
                    Log.i("ApplyLeaveFragment", till_date)
                    Log.i("ApplyLeaveFragment", date_halfday)
                    Log.i("ApplyLeaveFragment", reason_text)
                    leave = Leave(
                        leaveType = leave_type,
                        numOfDays = leave_numofdays,
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
    inner class MinMaxFilter() : InputFilter {
        private var intMin: Int = 0
        private var intMax: Int = 0

        // Initialized
        constructor(minValue: Int, maxValue: Int) : this() {
            this.intMin = minValue
            this.intMax = maxValue
        }

        override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dStart: Int, dEnd: Int): CharSequence? {
            try {
                val input = Integer.parseInt(dest.toString() + source.toString())
                if (isInRange(intMin, intMax, input)) {
                    return null
                }
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            }
            return ""
        }
        private fun isInRange(a: Int, b: Int, c: Int): Boolean {
            return if (b > a) c in a..b else c in b..a
        }
    }

}