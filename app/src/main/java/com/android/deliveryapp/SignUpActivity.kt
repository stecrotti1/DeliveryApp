package com.android.deliveryapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.deliveryapp.databinding.ActivitySignUpBinding
import com.android.deliveryapp.profile.ClientProfileActivity
import com.android.deliveryapp.util.Keys.Companion.isRegistered
import com.android.deliveryapp.util.Keys.Companion.userInfo
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore

    private val TAG = "EmailPassword"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance() // saves user data in cloud

        binding.nextButton.setOnClickListener {
            signUpUser(binding.email, binding.password, binding.confirmPassword)

        }
    }

    /**
     * @param email given by the user
     * @param password given by the user
     * @param confirmPwd must be the same as password in order to confirm
     */
    private fun signUpUser(
        email: TextInputEditText,
        password: TextInputEditText,
        confirmPwd: TextInputEditText
    ){
        if (email.text.isNullOrEmpty()) {
            email.error = getString(R.string.empty_email)
            email.requestFocus()
            return
        }
        if (password.text.isNullOrEmpty()) {
            password.error = getString(R.string.empty_password)
            password.requestFocus()
            return
        }

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
            confirmPwd.error = getString(R.string.password_not_match)
            confirmPwd.requestFocus()
            return
        }

        auth.createUserWithEmailAndPassword(email.text.toString(), password.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "createUserWithEmail: SUCCESS")

                    val sharedPreferences = getSharedPreferences(userInfo, Context.MODE_PRIVATE)

                    val editor = sharedPreferences.edit()
                    editor.putBoolean(isRegistered, true) // user is not flagged as registered
                    editor.apply()

                    startActivity(Intent(this@SignUpActivity, ClientProfileActivity::class.java))
                    finish()

                } else {
                    Log.w(TAG, "createUserWithEmail: FAILURE", task.exception)
                    Toast.makeText(baseContext, getString(R.string.sign_up_failure), Toast.LENGTH_SHORT).show()
                }
            }
        return
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


