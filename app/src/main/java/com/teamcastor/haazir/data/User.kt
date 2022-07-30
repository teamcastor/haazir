package com.teamcastor.haazir.data

data class User(
    // We don't know its format, so lets roll with String
    var empNumber: String,
    var email: String,
    var name: String,
    var gender: String,
    var phoneNumber: CharSequence,
    var address: String,
) {

}
