package com.teamcastor.haazir

import android.os.Build
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.teamcastor.haazir.databinding.FragmentApplyLeaveBinding
import java.text.SimpleDateFormat
import java.util.*


class ApplyLeaveFragment : Fragment() {

    var half: RadioButton? = null
    var regular: RadioButton? = null
    var numofdays: LinearLayout? = null
    var numofdaysedit: EditText? = null
    var fromdate: TextView? = null
    var from: LinearLayout? = null
    var fromtext: TextView? = null
    var till: LinearLayout? = null
    var tilldate: TextView? = null
    private var _binding: FragmentApplyLeaveBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?
    ): View? {
        val materialDateBuilder: MaterialDatePicker.Builder<*> =
            MaterialDatePicker.Builder.datePicker()
        materialDateBuilder.setTitleText("SELECT A DATE")
        val materialDatePicker = materialDateBuilder.build()
        val materialDatePicker1 = materialDateBuilder.build()

        _binding = FragmentApplyLeaveBinding.inflate(inflater,container,false)
        from = binding.fromdate
        fromdate = binding.fromdatetext
        till = binding.tilldate
        tilldate = binding.tilldatetext
        numofdays = binding.numofdays
        numofdaysedit = binding.numofdaysedit
        fromtext = binding.from

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
        numofdaysedit!!.filters = arrayOf<InputFilter>(MinMaxFilter(1, 7))

//        numedit = binding.numedittext
        half = binding.halfDay
        regular = binding.regular

        half!!.setOnClickListener{
//            numofdays?.visibility = View.INVISIBLE
//            numedit?.visibility = View.INVISIBLE
            numofdays?.isEnabled = false
            numofdaysedit?.isEnabled = false
            tilldate?.isEnabled = false
            fromtext!!.text = "Date"
        }
        regular!!.setOnClickListener(){
            numofdays?.isEnabled = true
            numofdaysedit?.isEnabled = true
            tilldate?.isEnabled = true
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