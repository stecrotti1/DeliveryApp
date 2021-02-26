package com.android.deliveryapp.manager

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.deliveryapp.R
import com.android.deliveryapp.databinding.ActivityRidersListBinding
import com.android.deliveryapp.manager.adapters.RiderListArrayAdapter
import com.android.deliveryapp.util.Keys.Companion.riderStatus
import com.android.deliveryapp.util.Keys.Companion.riders
import com.android.deliveryapp.util.RiderListItem
import com.google.firebase.firestore.FirebaseFirestore

class RidersListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRidersListBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var riderList: Array<RiderListItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRidersListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // show a back arrow button in actionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        firestore = FirebaseFirestore.getInstance()

        fetchRiderList(firestore)
    }

    private fun fetchRiderList(firestore: FirebaseFirestore) {
        riderList = emptyArray()

        firestore.collection(riders).get()
                .addOnSuccessListener { result ->
                    var email: String
                    var isAvailable: Boolean

                    for (document in result.documents) {
                        email = document.id
                        isAvailable = document.getBoolean(riderStatus) as Boolean

                        riderList = riderList.plus(RiderListItem(email, isAvailable))
                    }

                    updateView()
                }
                .addOnFailureListener { e ->
                    Log.w("FIREBASE_FIRESTORE", "Error getting riders data", e)

                    Toast.makeText(baseContext,
                            getString(R.string.error_getting_riders),
                            Toast.LENGTH_LONG).show()
                }
    }

    private fun updateView() {
        if (riderList.isNotEmpty()) {
            binding.ridersList.visibility = View.VISIBLE
            binding.empty.visibility = View.INVISIBLE

            binding.ridersList.adapter = RiderListArrayAdapter(this,
                    R.layout.rider_list_element,
                    riderList)
        } else {
            binding.empty.visibility = View.VISIBLE
            binding.ridersList.visibility = View.INVISIBLE
        }
    }

    // when the back button is pressed in actionbar, finish this activity
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}