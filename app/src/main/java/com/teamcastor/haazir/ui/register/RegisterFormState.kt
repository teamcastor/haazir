package com.teamcastor.haazir.ui.register

/**
 * Data validation state of the login form.
 */
data class RegisterFormState(
    val employeeNumberError: Int? = null,
    val emailError: Int? = null,
    val nameError: Int? = null,
    val passwordError: Int? = null,
    val genderError: Int? = null,
    val phoneError: Int? = null,
    val addressError: Int? = null,
    val isDataValid: Boolean = false
)