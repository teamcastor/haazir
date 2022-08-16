package com.teamcastor.haazir

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.teamcastor.haazir.content.AttendanceHistoryContent.AttendanceItem
import com.teamcastor.haazir.databinding.FragmentHistoryBinding

/**
 * [RecyclerView.Adapter] that can display a [AttendanceItem].
 */
class AttendanceHistoryRecyclerViewAdapter(
    private var values: List<AttendanceItem>
) : RecyclerView.Adapter<AttendanceHistoryRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentHistoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }
    fun updateData(values: List<AttendanceItem>) {
        this.values = values
        // TODO: Don't do this, only change altered positions
        notifyDataSetChanged()
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.dayNum.text = item.date.toString()
        holder.dayName.text = item.day
        holder.enterTime.text = item.inTime
        holder.workTime.text = item.workTime
        holder.exitTime.text = item.outTime
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val dayNum = binding.dayNum
        val dayName = binding.dayName
        val enterTime = binding.enterTime
        val workTime = binding.workDuration
        val exitTime = binding.exitTime
    }

}