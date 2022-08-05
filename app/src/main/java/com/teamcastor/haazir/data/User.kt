package com.teamcastor.haazir.data

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    // We don't know its format, so lets roll with String
    var empNumber: String? = null,
    var email: String? = null,
    var name: String? = null,
    var gender: String? = null,
    var phoneNumber: String? = null,
    var address: String? = null,
) {

}
