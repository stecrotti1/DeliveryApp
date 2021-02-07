package com.android.deliveryapp.profile

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.android.deliveryapp.R
import com.android.deliveryapp.databinding.ActivityManagerProfileBinding
import com.android.deliveryapp.home.ManagerHomeActivity
import com.google.firebase.auth.FirebaseAuth

class ManagerProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManagerProfileBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManagerProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        supportActionBar?.title = getString(R.string.manager)

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
                startActivity(Intent(this@ManagerProfileActivity, ManagerHomeActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}