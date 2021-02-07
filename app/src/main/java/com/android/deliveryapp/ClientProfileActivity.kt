package com.android.deliveryapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.android.deliveryapp.databinding.ActivityClientProfileBinding
import com.android.deliveryapp.home.ClientHomeActivity
import com.android.deliveryapp.util.Keys.Companion.clientAddress
import com.android.deliveryapp.util.Keys.Companion.hasLocation
import com.android.deliveryapp.util.Keys.Companion.userInfo
import com.android.deliveryapp.util.Keys.Companion.users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.IOException

class ClientProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClientProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var database: FirebaseFirestore

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()

        supportActionBar?.title = getString(R.string.client)

        var geocoder: List<Address>? = null

        val user = firebaseAuth.currentUser

        if (user != null) {
            binding.email.setText(user.email) // show email at the user
            binding.email.keyListener = null // not editable by user, but still visible
        }

        sharedPreferences = getSharedPreferences(userInfo, Context.MODE_PRIVATE)

        binding.location.keyListener = null // not editable by user, but still visible

        // if user has already set the location
        if (sharedPreferences.getBoolean(hasLocation, false) && user != null) {
            binding.setLocationBtn.visibility = View.INVISIBLE

            database.collection(users)
                    .get()
                    .addOnSuccessListener { result ->
                        for (document in result) {
                            if (document.id == user.email) {
                                val clientGeoPoint = document.getGeoPoint(clientAddress)

                                if (clientGeoPoint != null) {
                                    try {
                                        geocoder = Geocoder(this).getFromLocation(clientGeoPoint.latitude,
                                                clientGeoPoint.longitude,
                                                1)
                                    } catch (e: IOException) {
                                        Log.w("Geocoder", e.message.toString())
                                    }

                                    if (geocoder != null) {
                                        binding.location.setText("${geocoder!![0].getAddressLine(0)}, " +
                                                "${geocoder!![0].getAddressLine(1)}, " +
                                                "${geocoder!![0].adminArea}, " +
                                                geocoder!![0].postalCode)
                                    }
                                }
                            }
                        }
                    }
        } else {
            binding.setLocationBtn.visibility = View.VISIBLE
        }

        binding.setLocationBtn.setOnClickListener {
            startActivity(Intent(this@ClientProfileActivity, ClientLocationActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.profile_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.homePage  -> {
                if (binding.location.text.isNullOrEmpty() || sharedPreferences.getBoolean(hasLocation, true)) {
                    binding.location.error = getString(R.string.empty_location)
                    binding.location.requestFocus()
                }
                else {
                    startActivity(Intent(this@ClientProfileActivity, ClientHomeActivity::class.java))
                    finish()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
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