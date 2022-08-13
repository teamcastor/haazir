package com.teamcastor.haazir

import android.os.Build
import android.os.Bundle
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.Spanned
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.teamcastor.haazir.databinding.FragmentApplyLeaveBinding
import java.util.*


class ApplyLeaveFragment : Fragment() {

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
//        numofdaysedit?.isEnabled = false  // initialized before Type selected
//        till?.isEnabled = false
//        from?.isEnabled = false
//        tilldate?.isEnabled = true
//        fromdate?.isEnabled = true
//        reasontext?.isEnabled = false

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
//            numofdays?.isEnabled = false
//            numofdaysedit?.text = null
//            numofdaysedit?.isEnabled = false
//            tilldate?.isEnabled = false
//            till?.isEnabled = false
//            from?.isEnabled = true
            tilldate?.text = " Pick Date "
            fromtext!!.text = "Date"
        }
        regular!!.setOnClickListener(){
            numofdays?.visibility = View.VISIBLE
            from?.visibility = View.VISIBLE
            till?.visibility =  View.VISIBLE
            date?.visibility = View.INVISIBLE
//            numofdays?.isEnabled = true
//            till?.isEnabled = true
////            from?.isEnabled = true
//            numofdaysedit?.isEnabled = true
//            tilldate?.isEnabled = true
            fromtext!!.text = "From"

        }
        apply!!.setOnClickListener(){
            var i=0
            while (i!=1){
                if(regular!!.isChecked){
                    leave_type += "Regular"
                }
                else if (half!!.isChecked){
                    leave_type += "Half Day"
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
                    leave_numofdays += " " + numofdaysedit!!.text.toString()
                }
                if(regular!!.isChecked && fromdate!!.text == " Pick Date  "){
                    Toast.makeText(activity,"Select Date !!",Toast.LENGTH_LONG).show()
                    break
                }
                else if(regular!!.isChecked && fromdate!!.text != " Pick Date  "){
                    from_date += "" + fromdate!!.text.toString()
                }
                if(regular!!.isChecked && tilldate!!.text == " Pick Date  "){
                    Toast.makeText(activity,"Select Dates !!",Toast.LENGTH_LONG).show()
                    break
                }
                else if(regular!!.isChecked && tilldate!!.text != " Pick Date  "){
                    till_date += " " + tilldate!!.text.toString()
                }
                if(half!!.isChecked && datehalf!!.text == " Pick Date  "){
                    Toast.makeText(activity,"Select Date !!",Toast.LENGTH_LONG).show()
                    break
                }
                else if(half!!.isChecked && fromdate!!.text != " Pick Date  "){
                    date_halfday += " " + datehalf!!.text.toString()
                }
                if(TextUtils.isEmpty(reasontext!!.text.toString())){
                    Toast.makeText(activity,"Enter reason in brief",Toast.LENGTH_LONG).show()
                    break
                }
                else if (!TextUtils.isEmpty(reasontext!!.text.toString())){
                    reason_text += " " + reasontext!!.text.toString()
//                    Toast.makeText(activity,"gggg",Toast.LENGTH_LONG).show()
                    Log.i("ApplyLeaveFragment", leave_type)
                    Log.i("ApplyLeaveFragment", leave_numofdays)
                    Log.i("ApplyLeaveFragment", from_date)
                    Log.i("ApplyLeaveFragment", till_date)
                    Log.i("ApplyLeaveFragment", date_halfday)
                    Log.i("ApplyLeaveFragment", reason_text)
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