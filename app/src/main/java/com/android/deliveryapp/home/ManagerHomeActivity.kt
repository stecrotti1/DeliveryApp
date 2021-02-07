package com.android.deliveryapp.home

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.android.deliveryapp.R
import com.android.deliveryapp.databinding.ActivityManagerHomeBinding
import com.google.firebase.firestore.FirebaseFirestore

class ManagerHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManagerHomeBinding
    private lateinit var database: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManagerHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseFirestore.getInstance()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.manager_home_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.managerProfile -> {
                // TODO: 07/02/2021 return to profile activity
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}