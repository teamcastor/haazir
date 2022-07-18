package com.teamcastor.haazir.data.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.teamcastor.haazir.R
import com.teamcastor.haazir.data.FirebaseUserLiveData
import com.teamcastor.haazir.data.User
import com.teamcastor.haazir.ui.login.LoggedInUserView
import com.teamcastor.haazir.ui.login.LoginFormState
import com.teamcastor.haazir.ui.register.RegisterFormState
import com.teamcastor.haazir.ui.register.RegisterFragment

class LoginViewModel() : ViewModel() {
    companion object {
        fun logout() {
            Firebase.auth.signOut()
        }
    }
    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm
    private val _registerForm = MutableLiveData<RegisterFormState>()
    val registerFormState: LiveData<RegisterFormState> = _registerForm

    enum class AuthenticationState {
        AUTHENTICATED, UNAUTHENTICATED, INVALID_AUTHENTICATION
    }

    val authenticationState = FirebaseUserLiveData().map { user ->
        if (user != null) {
            AuthenticationState.AUTHENTICATED
        } else {
            AuthenticationState.UNAUTHENTICATED
        }
    }

    fun login(email: String, password: String) {
        Firebase.auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("TAG", "signInWithEmail:success")
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("TAG", "signInWithEmail:failure", task.exception)
                }
            }
    }

    fun register(user: User, password: String) {
        try {
            Firebase.auth.createUserWithEmailAndPassword(user.email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = Firebase.auth.currentUser?.uid
                        val db =
                            Firebase.database("https://haazir-11bae-default-rtdb.asia-southeast1.firebasedatabase.app/").reference
                        if (uid != null) {
                            db.child("users").child(uid).setValue(user)
                                .addOnCompleteListener { t ->
                                    if (t.isSuccessful) {
                                        println("Registeration data uploaded")
                                    } else
                                        println("Registeration failed ${t.exception}")
                                }
                        }
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("TAG", "createUserWithEmail:success")
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("TAG", "createUserWithEmail:failure", task.exception)
                    }
                }
        } catch (e: Throwable) {
            println("Error registering in")
        }
    }


    fun loginDataChanged(username: String, password: String) {
        if (!RegisterFragment.isEmailValid(username)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_email)
        } else if (!RegisterFragment.isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    fun registerDataChanged(user: User, password: String) {
        if (!RegisterFragment.isEmailValid(user.email)) {
            _registerForm.value = RegisterFormState(emailError = R.string.invalid_email)
        } else if (!RegisterFragment.isNameValid(user.name)) {
            _registerForm.value = RegisterFormState(nameError = R.string.invalid_name)
        } else if (!RegisterFragment.isGenderValid(user.gender)) {
            _registerForm.value = RegisterFormState(genderError = R.string.invalid_gender)
        } else if (!RegisterFragment.isNumberValid(user.phoneNumber)) {
            _registerForm.value = RegisterFormState(phoneError = R.string.invalid_number)
        } else if (!RegisterFragment.isPasswordValid(password)) {
            _registerForm.value = RegisterFormState(passwordError = R.string.invalid_password)
        } else if (!RegisterFragment.isAddressValid(user.address)) {
            _registerForm.value = RegisterFormState(addressError = R.string.invalid_address)
        } else {
            _registerForm.value = RegisterFormState(isDataValid = true)
        }
    }
}