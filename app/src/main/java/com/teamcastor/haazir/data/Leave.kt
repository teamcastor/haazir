package com.teamcastor.haazir.data

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Leave (
    var leaveType: String? = null,
    var numOfDays: String? = null,
    var fromDate: String? = null,
    var toDate: String? = null,
    var halfDate: String? = null,
    var reason: String? = null,
)