package com.android.deliveryapp

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.android.deliveryapp.databinding.ActivitySelectUserTypeBinding
import com.android.deliveryapp.util.Keys.Companion.CLIENT
import com.android.deliveryapp.util.Keys.Companion.MANAGER
import com.android.deliveryapp.util.Keys.Companion.RIDER
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

            editor.apply()
            // frame?
            TODO("startActivity(Intent(this@SelectionActivity, SignupActivity::class.java))")
        }
    }
}