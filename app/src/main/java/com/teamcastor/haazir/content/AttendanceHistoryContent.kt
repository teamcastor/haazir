package com.teamcastor.haazir.content

import com.teamcastor.haazir.data.Attendance
import com.teamcastor.haazir.toDate
import com.teamcastor.haazir.toDay
import com.teamcastor.haazir.toDurationHM
import com.teamcastor.haazir.toTimeIST

object AttendanceHistoryContent {


    private val ITEM_MAP: MutableMap<String, AttendanceItem> = HashMap()

    val ITEMS: List<AttendanceItem> get() =  ArrayList<AttendanceItem>(ITEM_MAP.values)


    fun addItem(date: String, at: Attendance?) {
        val item = createAttendanceItem(date, at)
        ITEM_MAP[date] = item

    }

    private fun createAttendanceItem(date: String, at: Attendance?): AttendanceItem {
        return AttendanceItem(
            date.toLong().toDate().toInt(),
            date.toLong().toDay(),
            at?.checkIn?.toTimeIST() ?: "--:--",
            (at?.checkIn?.let { at.checkOut?.minus(it) })?.toDurationHM() ?: "--:--",
            at?.checkOut?.toTimeIST() ?: "--:--"
        )
    }

    data class AttendanceItem(val date: Int, val day: String, val inTime: String, val workTime: String, val outTime: String)
}