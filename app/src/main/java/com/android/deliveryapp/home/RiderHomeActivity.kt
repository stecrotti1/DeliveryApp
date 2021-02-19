package com.android.deliveryapp.home

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.android.deliveryapp.R
import com.android.deliveryapp.databinding.ActivityRiderHomeBinding
import com.android.deliveryapp.profile.RiderProfileActivity
import com.google.firebase.firestore.FirebaseFirestore

class RiderHomeActivity : AppCompatActivity() {
    // TODO: 19/02/2021 notifications 
    private lateinit var binding: ActivityRiderHomeBinding
    private lateinit var database: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRiderHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseFirestore.getInstance()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.rider_home_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.riderProfile -> {
                startActivity(Intent(this@RiderHomeActivity, RiderProfileActivity::class.java))
                true
            }
            R.id.riderDeliveries -> {
                // TODO: 19/02/2021 history deliveries 
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}