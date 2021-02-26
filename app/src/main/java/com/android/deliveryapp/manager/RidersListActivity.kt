package com.android.deliveryapp.manager

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.deliveryapp.R
import com.android.deliveryapp.databinding.ActivityRidersListBinding
import com.android.deliveryapp.manager.adapters.RiderListArrayAdapter
import com.android.deliveryapp.util.Keys.Companion.clientAddress
import com.android.deliveryapp.util.Keys.Companion.clients
import com.android.deliveryapp.util.Keys.Companion.riderStatus
import com.android.deliveryapp.util.Keys.Companion.riders
import com.android.deliveryapp.util.RiderListItem
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint

class RidersListActivity : AppCompatActivity() {
    // TODO: 26/02/2021 get client email from intent
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

    override fun onStart() {
        super.onStart()

        binding.ridersList.setOnItemClickListener { _, _, i, _ ->
            if (!riderList[i].availability) { // if rider is not available, show an alert dialog
                showUnavailabilityDialog()
            } else {

                sendOrderToRider(firestore, riderList[i].email)
            }
        }
    }

    private fun getClientAddress(firestore: FirebaseFirestore): GeoPoint {
        var geoPoint = GeoPoint(0.0, 0.0)

        val email = intent.getStringExtra("clientEmail")

        if (email != null) {
            firestore.collection(clients).document(email)
                    .get()
                    .addOnSuccessListener { result ->
                        geoPoint = result.getGeoPoint(clientAddress) as GeoPoint
                    }
                    .addOnFailureListener { e ->
                        Log.w("FIREBASE_FIRESTORE", "Error getting client address", e)

                        Toast.makeText(baseContext,
                                getString(R.string.error_user_data),
                                Toast.LENGTH_LONG).show()
                    }
        }

        return geoPoint
    }

    private fun sendOrderToRider(firestore: FirebaseFirestore, rider: String) {
        // TODO: 26/02/2021 send client position
        // TODO: 26/02/2021 send total price 
        // TODO: 26/02/2021 send products and their quantity 
    }

    private fun showUnavailabilityDialog() {
        val dialog: AlertDialog?

        val dialogView = LayoutInflater.from(this).inflate(R.layout.driver_unavailable_dialog, null)

        val dialogBuilder = AlertDialog.Builder(this)
                .setView(dialogView)

        val okBtn: ExtendedFloatingActionButton = dialogView.findViewById(R.id.okBtn)

        dialog = dialogBuilder.create()
        dialog.show()

        okBtn.setOnClickListener {
            dialog.dismiss()
        }
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