package com.android.deliveryapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.android.deliveryapp.home.ClientHomeActivity
import com.android.deliveryapp.home.ManagerHomeActivity
import com.android.deliveryapp.home.RiderHomeActivity
import com.android.deliveryapp.util.Keys.Companion.CLIENT
import com.android.deliveryapp.util.Keys.Companion.MANAGER
import com.android.deliveryapp.util.Keys.Companion.RIDER
import com.android.deliveryapp.util.Keys.Companion.isLogged
import com.android.deliveryapp.util.Keys.Companion.isRegistered
import com.android.deliveryapp.util.Keys.Companion.userInfo
import com.android.deliveryapp.util.Keys.Companion.userType

/**
 * Splash screen activity
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide() // hide action bar

        Handler(Looper.getMainLooper()).postDelayed({
            val sharedPreferences = getSharedPreferences(userInfo, Context.MODE_PRIVATE)

            if (sharedPreferences.getBoolean(isRegistered, false)) {
                if (sharedPreferences.getBoolean(isLogged, false)) {
                    when (sharedPreferences.getString(userType, null)) {
                        CLIENT -> startActivity(Intent(this@MainActivity, ClientHomeActivity::class.java))
                        RIDER -> startActivity(Intent(this@MainActivity, RiderHomeActivity::class.java))
                        MANAGER -> startActivity(Intent(this@MainActivity, ManagerHomeActivity::class.java))
                        else -> startActivity(Intent(this@MainActivity, SelectUserTypeActivity::class.java))
                    }
                } else {
                    startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                }
            } else {
                startActivity(Intent(this@MainActivity, SelectUserTypeActivity::class.java))
            }
            finish()
        }, 1500) // wait 1.5 seconds, then show the activity

    }
}