package com.android.deliveryapp.home

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.android.deliveryapp.R
import com.android.deliveryapp.databinding.ActivityManagerHomeBinding
import com.android.deliveryapp.profile.ManagerProfileActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ManagerHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManagerHomeBinding
    private lateinit var database: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManagerHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseFirestore.getInstance()

        storage = FirebaseStorage.getInstance()


        // TODO: 19/02/2021 add product quantity
        
        binding.addProductButton.setOnClickListener {
            // TODO: 19/02/2021 add product activity 
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.manager_home_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.managerProfile -> {
                startActivity(Intent(this@ManagerHomeActivity, ManagerProfileActivity::class.java))
                true
            }
            R.id.ridersList -> {
                // TODO: 19/02/2021 activity list riders
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}