package com.teamcastor.haazir

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment


class ApplyLeaveFragment : Fragment() {

    var ch1: CheckBox? = null
    var ch2: CheckBox? = null
    var numofdays: TextView? = null
    var numedit: EditText? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        ch1 = view?.findViewById(R.id.half_day)
        ch2 = view?.findViewById(R.id.regular)
        numofdays = view?.findViewById(R.id.numofdays)
        numedit = view?.findViewById(R.id.numedittext)
        ch1?.setOnClickListener() { view ->
            ch2!!.isChecked = false
            System.out.println("half")
        }
        ch2?.setOnClickListener(){ view ->
            ch1!!.isChecked = false
            System.out.println("reg")
        }
//        fun itemClicked(view: View): Void?{
//            if(ch1?.isChecked == true){
//                numofdays?.keyListener=null
//                numedit?.keyListener=null
//            }
//        }
//        public void itemClicked(View v) {
//            //code to check if this checkbox is checked!
//            CheckBox checkBox = (CheckBox)v;
//            if(checkBox.isChecked()){
//
//            }
//        }
//        if(ch1?.isChecked == true){
//            numofdays?.keyListener=null
//            numedit?.keyListener=null
//        }
        return inflater.inflate(R.layout.fragment_apply_leave, container, false)
    }
//    fun half() {
//        ch2!!.setChecked(false)
//        System.out.println("half")
////        ch2!!.jumpDrawablesToCurrentState()
//    }
////
//    fun regular() {
//        ch1!!.setChecked(false)
//    System.out.println("reg")
////        ch1!!.jumpDrawablesToCurrentState()
//    }
}