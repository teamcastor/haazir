package com.teamcastor.haazir.data.model

import android.util.Log
import androidx.lifecycle.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.teamcastor.haazir.R
import com.teamcastor.haazir.data.FirebaseUserLiveData
import com.teamcastor.haazir.data.ScanResult
import com.teamcastor.haazir.data.User
import com.teamcastor.haazir.ui.login.LoggedInUserView
import com.teamcastor.haazir.ui.login.LoginFormState
import com.teamcastor.haazir.ui.login.LoginResult
import com.teamcastor.haazir.ui.register.RegisterFormState
import com.teamcastor.haazir.ui.register.RegisterFragment
import java.lang.Exception


class LoginViewModel() : ViewModel() {
    companion object {
        fun logout() {
            Firebase.auth.signOut()
        }
        const val TAG: String = "LoginViewModel"
        val db =
            Firebase.database("https://haazir-11bae-default-rtdb.asia-southeast1.firebasedatabase.app/").reference
    }

    val user: MutableLiveData<User> = MutableLiveData()

    fun getUser(): LiveData<User> {
        if (user.value == null) {
            val userListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    user.postValue(dataSnapshot.getValue<User>())
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w(TAG, "loadUser:onCancelled", databaseError.toException())
                }
            }
            Firebase.auth.uid?.let { db.child("users").child(it).addListenerForSingleValueEvent(userListener) }
        }

        return user
    }

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm
    private val _loginResult = MutableLiveData<LoginResult>()
    private val _scanResult = MutableLiveData<ScanResult>()
    val scanResult: LiveData<ScanResult> = _scanResult
    val loginResult: LiveData<LoginResult> = _loginResult
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

    fun authenticate(isSharp: Boolean, isSpoof: Boolean, isRecognized: Boolean) {
        _scanResult.value = ScanResult(isSharp, isSpoof, isRecognized)
    }

    fun findEmail(en: String, password: String) {
        try {
            db.child("pairsUE").child(en).addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            val email = dataSnapshot.value.toString()
                            login(email, password)
                        } else {
                            _loginResult.value = LoginResult(error = R.string.login_failed)
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "This employee number is not registered")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.w(TAG, "getUser:onCancelled")
                    }
                })
        } catch (e: Exception) {
            Log.w(TAG, "Could not initiate email lookup", e)
            _loginResult.value = LoginResult(error = R.string.login_failed)
        }
    }

    fun login(email : String, password: String) {
        try {
            Firebase.auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _loginResult.value =
                            LoginResult(success = Firebase.auth.currentUser?.email?.let {
                                LoggedInUserView(
                                    displayName = it
                                )
                            })
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success")
                    } else {
                        _loginResult.value = LoginResult(error = R.string.login_failed)
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                    }
                }
        } catch (e: Throwable) {
            _loginResult.value = LoginResult(error = R.string.login_failed)
            Log.w(TAG, "signInWithEmail:exception", e)
        }
    }

    fun register(user: User, password: String) {
        try {
            // We have to make sure no other account exists with this Emp Number
            db.child("pairsUE").addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (!dataSnapshot.hasChild(user.empNumber!!)) {
                            Firebase.auth.createUserWithEmailAndPassword(user.email!!, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val uid = Firebase.auth.currentUser?.uid

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
                                        Log.d(TAG, "createUserWithEmail:success")
                                        // Upload email and empNumber pair to pairsUE ref
                                        db.child("pairsUE").child(user.empNumber!!).setValue(user.email)
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                                    }
                                }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException())
                    }
                })


        } catch (e: Throwable) {
            Log.w(TAG, "createUserWithEmail:exception", e)
        }
    }


    fun loginDataChanged(username: String, password: String) {
        if (!RegisterFragment.isEmpNumValid(username)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_employee_number)
        } else if (!RegisterFragment.isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    fun registerDataChanged(user: User, password: String) {
        if (!RegisterFragment.isEmpNumValid(user.empNumber!!)) {
            _registerForm.value = RegisterFormState(employeeNumberError = R.string.invalid_employee_number)
        }
        else if (!RegisterFragment.isEmailValid(user.email!!)) {
            _registerForm.value = RegisterFormState(emailError = R.string.invalid_email)
        } else if (!RegisterFragment.isNameValid(user.name!!)) {
            _registerForm.value = RegisterFormState(nameError = R.string.invalid_name)
        } else if (!RegisterFragment.isGenderValid(user.gender!!)) {
            _registerForm.value = RegisterFormState(genderError = R.string.invalid_gender)
        } else if (!RegisterFragment.isNumberValid(user.phoneNumber!!)) {
            _registerForm.value = RegisterFormState(phoneError = R.string.invalid_number)
        } else if (!RegisterFragment.isPasswordValid(password)) {
            _registerForm.value = RegisterFormState(passwordError = R.string.invalid_password)
        } else if (!RegisterFragment.isAddressValid(user.address!!)) {
            _registerForm.value = RegisterFormState(addressError = R.string.invalid_address)
        } else {
            _registerForm.value = RegisterFormState(isDataValid = true)
        }
    }
}
