package com.teamcastor.haazir.content

import com.teamcastor.haazir.data.Leave
import com.teamcastor.haazir.toHumanDate

object LeaveHistoryContent {


    val ITEM_MAP: MutableMap<String, LeaveItem> = HashMap()

    val ITEMS: List<LeaveItem> get() = ArrayList<LeaveItem>(ITEM_MAP.values)


    fun addItem(date: String, leave: Leave?) {
        val item = createLeaveItem(date, leave)
        ITEM_MAP[date] = item

    }

    private fun createLeaveItem(date: String, leave: Leave?): LeaveItem {
        return LeaveItem(
            leave?.halfDate?.toHumanDate()
                ?: (leave?.fromDate?.toHumanDate() + " to " + leave?.toDate?.toHumanDate()),
            leave?.leaveType,
            leave?.status
        )
    }

    data class LeaveItem(val date: String?, val type: String?, val status: String?)
}