package com.android.deliveryapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.deliveryapp.databinding.ActivityLoginBinding
import com.android.deliveryapp.home.ClientHomeActivity
import com.android.deliveryapp.home.ManagerHomeActivity
import com.android.deliveryapp.home.RiderHomeActivity
import com.android.deliveryapp.util.Keys.Companion.CLIENT
import com.android.deliveryapp.util.Keys.Companion.MANAGER
import com.android.deliveryapp.util.Keys.Companion.RIDER
import com.android.deliveryapp.util.Keys.Companion.isLogged
import com.android.deliveryapp.util.Keys.Companion.userInfo
import com.android.deliveryapp.util.Keys.Companion.userType
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    private val TAG = "EmailPassword"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        sharedPreferences = getSharedPreferences(userInfo, Context.MODE_PRIVATE)

        binding.submitButton.setOnClickListener {
            loginUser(binding.loginEmail, binding.loginPassword)
        }
    }

    private fun loginUser(email: TextInputEditText, password: TextInputEditText) {
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

        val editor = sharedPreferences.edit()
        editor.putBoolean(isLogged, binding.rememberUser.isChecked) // set preference
        editor.apply()

        auth.signInWithEmailAndPassword(binding.loginEmail.text.toString(),
                binding.loginPassword.text.toString()).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "signInWithEmail:success")

                when (sharedPreferences.getString(userType, null)) {
                    CLIENT -> startActivity(Intent(this@LoginActivity, ClientHomeActivity::class.java))
                    RIDER -> startActivity(Intent(this@LoginActivity, RiderHomeActivity::class.java))
                    MANAGER -> startActivity(Intent(this@LoginActivity, ManagerHomeActivity::class.java))
                }
                finish()
            }
            else {
                Log.w(TAG, "signInWithEmail:failure", task.exception)
                Toast.makeText(baseContext, getString(R.string.login_failure), Toast.LENGTH_SHORT).show()
            }
        }

    }

    // hide keyboard when user clicks outside EditText
    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }
        return super.dispatchTouchEvent(event)
    }
}