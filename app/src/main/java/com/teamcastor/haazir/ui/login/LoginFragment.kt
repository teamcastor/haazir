package com.teamcastor.haazir.ui.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.teamcastor.haazir.R
import com.teamcastor.haazir.data.model.AppViewModel
import com.teamcastor.haazir.databinding.FragmentLoginBinding


class LoginFragment : Fragment(R.layout.fragment_login) {
    private val appViewModel: AppViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentLoginBinding.bind(view)
        val empNumberEditText = binding.empNumber
        val empNumberEditTextLayout = binding.empNumberLayout
        val passwordEditTextLayout = binding.passwordLayout
        val passwordEditText = binding.password
        val loadingProgressBar = binding.loading
        val loginButton = binding.login

        appViewModel.loginFormState.observe(
            viewLifecycleOwner,
            Observer { loginFormState ->
                if (loginFormState == null) {
                    return@Observer
                }
                loginButton.isEnabled = loginFormState.isDataValid
                if (loginFormState.passwordError != null) {
                    passwordEditTextLayout.error = getString(loginFormState.passwordError)
                } else {
                    passwordEditTextLayout.error = null
                }
                if (loginFormState.usernameError != null) {
                    empNumberEditTextLayout.error = getString(loginFormState.usernameError)
                }
                else {
                    empNumberEditTextLayout.error = null
                }

            })

        appViewModel.loginResult.observe(viewLifecycleOwner,
            Observer { loginResult ->
                loginResult ?: return@Observer
                loginResult.success?.let {
                    loadingProgressBar.visibility = View.GONE
                }
                loginResult.error?.let {
                    loadingProgressBar.visibility = View.GONE
                    showLoginFailed(it)
                }
            })

        val afterTextChangedListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // ignore
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // ignore
            }

            override fun afterTextChanged(s: Editable) {
                appViewModel.loginDataChanged(
                    empNumberEditText.text.toString(),
                    passwordEditText.text.toString()
                )
            }
        }
        empNumberEditText.addTextChangedListener(afterTextChangedListener)
        passwordEditText.addTextChangedListener(afterTextChangedListener)
        passwordEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                loadingProgressBar.visibility = View.VISIBLE
                appViewModel.findEmail(
                    empNumberEditText.text.toString(),
                    passwordEditText.text.toString()
                )
            }
            false
        }

        loginButton.setOnClickListener {
            loadingProgressBar.visibility = View.VISIBLE
            appViewModel.findEmail(
                empNumberEditText.text.toString(),
                passwordEditText.text.toString()
            )
        }
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, errorString, Toast.LENGTH_LONG).show()
    }
}