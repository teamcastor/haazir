package com.teamcastor.haazir.data.model

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.teamcastor.haazir.R
import com.teamcastor.haazir.data.FirebaseUserLiveData
import com.teamcastor.haazir.ui.login.LoggedInUserView
import com.teamcastor.haazir.ui.login.LoginFormState
import com.teamcastor.haazir.ui.login.LoginResult

class LoginViewModel : ViewModel() {
    companion object {
        fun logout() {
            Firebase.auth.signOut()
        }
    }

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

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
                    _loginResult.value =
                        LoginResult(success = Firebase.auth.currentUser?.email?.let {
                            LoggedInUserView(
                                displayName = it
                            )
                        })
                } else {
                    // If sign in fails, display a message to the user.
                    _loginResult.value = LoginResult(error = R.string.login_failed)
                    Log.w("TAG", "signInWithEmail:failure", task.exception)
                }
            }
    }

    fun register(email: String, password: String) {
        try {
            Firebase.auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
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
        if (!isUserNameValid(username)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return if (username.contains("@")) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
}