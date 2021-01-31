package com.android.deliveryapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.android.deliveryapp.databinding.ActivityProfileBinding
import com.android.deliveryapp.util.Keys.Companion.CLIENT
import com.android.deliveryapp.util.Keys.Companion.MANAGER
import com.android.deliveryapp.util.Keys.Companion.RIDER
import com.android.deliveryapp.util.Keys.Companion.clientLocation
import com.android.deliveryapp.util.Keys.Companion.userInfo
import com.android.deliveryapp.util.Keys.Companion.userType
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        val user = firebaseAuth.currentUser

        if (user != null) {
            binding.email.setText(user.email) // show email at the user
            binding.email.keyListener = null // not editable, but still visible
        }

        val sharedPreferences = getSharedPreferences(userInfo, Context.MODE_PRIVATE)

        when (sharedPreferences.getString(userType, null)) {
            CLIENT -> {
                supportActionBar?.title = getString(R.string.client) // set "Client" in action bar
                binding.riderStatus.visibility = View.INVISIBLE
                binding.location.hint = getString(R.string.location_hint)
                binding.location.setText(intent.getStringExtra(clientLocation)?:"")
            }
            RIDER -> {
                supportActionBar?.title = getString(R.string.rider) // set "Rider" in action bar
                binding.location.visibility = View.INVISIBLE
                binding.riderStatus.visibility = View.VISIBLE
            }
            MANAGER -> {
                supportActionBar?.title = getString(R.string.manager) // set "Manager" in action bar
                binding.riderStatus.visibility = View.INVISIBLE
                binding.location.hint = getString(R.string.market_location_hint)
            }
        }

        binding.isLocationCorrect.setOnClickListener {
            startActivity(Intent(this@ProfileActivity, LocationActivity::class.java))
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