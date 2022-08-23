package com.teamcastor.haazir.data

data class Leave (
    var leaveType: String? = null,
    var fromDate: Long? = null,
    var toDate: Long? = null,
    var halfDate: Long? = null,
    var reason: String? = null,
    var status: String? = null
)