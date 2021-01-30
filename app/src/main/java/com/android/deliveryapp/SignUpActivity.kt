package com.android.deliveryapp

import android.content.Context
import android.os.Bundle
import android.util.Patterns
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.android.deliveryapp.databinding.ActivitySignUpBinding
import com.google.android.material.textfield.TextInputEditText

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.nextButton.setOnClickListener {
            signUpUser(binding.email, binding.password, binding.confirmPassword)

        }
    }

    private fun signUpUser(
        email: TextInputEditText,
        password: TextInputEditText,
        confirmPwd: TextInputEditText
    ) {
        if (checkInput(binding.email)
            && checkInput(binding.password)
            && checkInput(binding.confirmPassword)) {

            if (!Patterns.EMAIL_ADDRESS.matcher(email.text.toString()).matches()) {
                email.error = getString(R.string.invalid_email)
                email.requestFocus()
                return
            }
            if (password.text!!.length < 6) {
                password.error = getString(R.string.invalid_password)
                password.requestFocus()
                return
            }
            if (password.text.toString() != confirmPwd.text.toString()) {
                password.error = getString(R.string.password_not_match)
                confirmPwd.error = getString(R.string.password_not_match)
                password.requestFocus()
                confirmPwd.requestFocus()
                return
            }
        }
    }

    private fun checkInput(inputText: TextInputEditText): Boolean {
        return if (inputText.text.isNullOrEmpty()) {
            inputText.error = getString(R.string.empty)
            inputText.requestFocus()
            false
        } else true
    }

    // hide keyboard when user clicks outside EditText
    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString("email", binding.email.text.toString())
        outState.putString("pwd", binding.password.text.toString())
        outState.putString("confirmPwd", binding.confirmPassword.text.toString())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        binding.email.setText(savedInstanceState.getString("email"))
        binding.password.setText(savedInstanceState.getString("pwd"))
        binding.confirmPassword.setText(savedInstanceState.getString("confirmPwd"))
    }
}


