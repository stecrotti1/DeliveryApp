package com.android.deliveryapp.manager

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.deliveryapp.databinding.ActivityRidersListBinding

class RidersListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRidersListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRidersListBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}