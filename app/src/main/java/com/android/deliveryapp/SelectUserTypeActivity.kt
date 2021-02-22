package com.android.deliveryapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.deliveryapp.databinding.ActivitySelectUserTypeBinding
import com.android.deliveryapp.util.Keys.Companion.CLIENT
import com.android.deliveryapp.util.Keys.Companion.MANAGER
import com.android.deliveryapp.util.Keys.Companion.RIDER
import com.android.deliveryapp.util.Keys.Companion.hasLocation
import com.android.deliveryapp.util.Keys.Companion.userInfo
import com.android.deliveryapp.util.Keys.Companion.userType

class SelectUserTypeActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelectUserTypeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectUserTypeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.confirmButton.setOnClickListener {

            val sharedPreferences = getSharedPreferences(userInfo, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()

            if (binding.client.isChecked) {
                editor.putString(userType, CLIENT)
            }
            if (binding.rider.isChecked) {
                editor.putString(userType, RIDER)
            }
            if (binding.manager.isChecked) {
                editor.putString(userType, MANAGER)
            }

            editor.putBoolean(hasLocation, false)

            editor.apply()

            startActivity(Intent(this@SelectUserTypeActivity, SignUpActivity::class.java))
            finish()
        }

        binding.hasAccount.setOnClickListener {
            startActivity(Intent(this@SelectUserTypeActivity, LoginActivity::class.java))
            finish()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putBoolean("clientRadioButton", binding.client.isChecked)
        outState.putBoolean("riderRadioButton", binding.rider.isChecked)
        outState.putBoolean("managerRadioButton", binding.manager.isChecked)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        binding.client.isChecked = savedInstanceState.getBoolean("clientRadioButton")
        binding.rider.isChecked = savedInstanceState.getBoolean("riderRadioButton")
        binding.manager.isChecked = savedInstanceState.getBoolean("managerRadioButton")
    }
}