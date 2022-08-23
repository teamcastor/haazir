package com.teamcastor.haazir

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.teamcastor.haazir.content.LeaveHistoryContent.LeaveItem
import com.teamcastor.haazir.databinding.LayoutLeaveStatusCardBinding

/**
 * [RecyclerView.Adapter] that can display a [LeaveItem].
 */
class LeaveHistoryRecyclerViewAdapter(
    private var values: List<LeaveItem>
) : RecyclerView.Adapter<LeaveHistoryRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            LayoutLeaveStatusCardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    fun updateData(values: List<LeaveItem>) {
        this.values = values
        // TODO: Don't do this, only change altered positions
        notifyDataSetChanged()
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.date.text = item.date
        holder.type.text = item.type
        holder.status.text = item.status
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: LayoutLeaveStatusCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val date = binding.date
        val type = binding.type
        val status = binding.status
    }

}