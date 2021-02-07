package com.android.deliveryapp.profile

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.android.deliveryapp.R
import com.android.deliveryapp.databinding.ActivityRiderProfileBinding
import com.android.deliveryapp.home.RiderHomeActivity
import com.google.firebase.auth.FirebaseAuth

class RiderProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRiderProfileBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRiderProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        supportActionBar?.title = getString(R.string.rider)

        val user = auth.currentUser

        if (user != null) {
            binding.email.setText(user.email) // show email at the user
            binding.email.keyListener = null // not editable by user, but still visible
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.profile_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.homePage -> {
                startActivity(Intent(this@RiderProfileActivity, RiderHomeActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}