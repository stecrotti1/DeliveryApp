package com.android.deliveryapp.home

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.android.deliveryapp.R
import com.android.deliveryapp.databinding.ActivityClientHomeBinding
import com.android.deliveryapp.profile.ClientProfileActivity

class ClientHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClientHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TODO: 19/02/2021 add product quantity 
        // TODO: 19/02/2021 insert images from firebase
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.client_home_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.clientProfile -> {
                startActivity(Intent(this@ClientHomeActivity, ClientProfileActivity::class.java))
                true
            }
            R.id.orders -> {
                // TODO: 07/02/2021 start activity orders or fragment??
                true
            }
            R.id.shoppingCart -> {
                // TODO: 07/02/2021 start activity shopping cart or fragment??
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}