package com.teamcastor.haazir.ui.register

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.teamcastor.haazir.data.model.LoginViewModel
import com.teamcastor.haazir.databinding.FragmentRegisterBinding
import androidx.lifecycle.Observer
import com.teamcastor.haazir.R
import com.teamcastor.haazir.data.User


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

//private val bindings get() = binding!!

class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private val loginViewModel: LoginViewModel by activityViewModels()
    private lateinit var user: User

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        val gender = resources.getStringArray(R.array.Gender)
        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.genderdropdown, gender)
        binding.genderpicker.setAdapter(arrayAdapter)
        return binding.root


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val emailEditText = binding.empid
        val nameEditText = binding.empName
        val genderEditText = binding.genderpicker
        var genderEditTextLayout = binding.genderLayout
        val addressEditText = binding.address
        val phoneNumberEditText = binding.number
        val registerButton = binding.register
        val passwordEditText = binding.password
        val passwordEditTextLayout = binding.passwordLayout

        loginViewModel.registerFormState.observe(viewLifecycleOwner,
            Observer { registerFormState ->
                if (registerFormState == null) {
                    return@Observer
                }
                println("The data is valid: $registerFormState.isDataValid")
                registerButton.isEnabled = registerFormState.isDataValid
                registerFormState.emailError?.let {
                    emailEditText.error = getString(it)
                }
                registerFormState.nameError?.let {
                    nameEditText.error = getString(it)
                }
                registerFormState.phoneError?.let {
                    phoneNumberEditText.error = getString(it)
                }
                // For a TextInputLayout the error is not set to null
                // on every keypress like TextInputEditText
                if (registerFormState.passwordError != null) {
                    passwordEditTextLayout.error = getString(registerFormState.passwordError)
                }
                else {
                    passwordEditTextLayout.error = null
                }

                registerFormState.addressError?.let {
                    addressEditText.error = getString(it)
                    println(addressEditText.error)
                }
                registerFormState.genderError?.let {
                    genderEditTextLayout.error = getString(it)
                }
            })

        val afterTextChangedListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // ignore
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                user = User(
                    email = emailEditText.text.toString(),
                    phoneNumber = phoneNumberEditText.text.toString(),
                    address = addressEditText.text.toString(),
                    name = nameEditText.text.toString(),
                    gender = genderEditText.text.toString()
                )
                loginViewModel.registerDataChanged(user, passwordEditText.text.toString())
            }
        }

        emailEditText.addTextChangedListener(afterTextChangedListener)
        passwordEditText.addTextChangedListener(afterTextChangedListener)
        phoneNumberEditText.addTextChangedListener(afterTextChangedListener)
        addressEditText.addTextChangedListener(afterTextChangedListener)
        nameEditText.addTextChangedListener(afterTextChangedListener)
        genderEditText.setOnItemClickListener { _,_,_,_ ->
           genderEditTextLayout.error = null
        }

        registerButton.setOnClickListener {
            loginViewModel.register(user, passwordEditText.text.toString())
        }
    }
    companion object {
        // A placeholder username validation check
        fun isEmailValid(username: String): Boolean {
            return Patterns.EMAIL_ADDRESS.matcher(username).matches()
        }
        fun isNameValid(username: String): Boolean {
            return (username.isNotEmpty())
        }
        fun isNumberValid(number: CharSequence): Boolean {
            return Patterns.PHONE.matcher(number).matches()
        }
        fun isAddressValid(address: String): Boolean {
            return (address.isNotEmpty())
        }

        // A placeholder password validation check
        fun isPasswordValid(password: String): Boolean {
            return password.length > 5
        }
        fun isGenderValid(gender: String): Boolean {
            return gender.isNotEmpty()
    }
    }

}