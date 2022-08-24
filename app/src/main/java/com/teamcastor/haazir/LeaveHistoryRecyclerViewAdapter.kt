package com.teamcastor.haazir

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.teamcastor.haazir.content.LeaveHistoryContent.LeaveItem
import com.teamcastor.haazir.databinding.LayoutLeaveStatusCardBinding
import kotlinx.coroutines.NonDisposableHandle.parent
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

        if(holder.status.text == "Approved"){
            holder.statuscard.setCardBackgroundColor(Color.parseColor("#BAF3D2"))
            holder.status.setTextColor(Color.parseColor("#5ba074"))
        }
        if(holder.status.text == "Awaiting"){
            holder.statuscard.setCardBackgroundColor(Color.parseColor("#ffefb9"))
            holder.status.setTextColor(Color.parseColor("#bfa450"))
        }
        if(holder.status.text == "Declined"){
            holder.statuscard.setCardBackgroundColor(Color.parseColor("#fdeeec"))
            holder.status.setTextColor(Color.parseColor("#d0a2a2"))
        }
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: LayoutLeaveStatusCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val date = binding.date
        val type = binding.type
        val status = binding.status
        val statuscard = binding.statuscard
    }

}