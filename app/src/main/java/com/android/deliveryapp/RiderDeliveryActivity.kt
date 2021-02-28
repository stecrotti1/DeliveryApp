package com.android.deliveryapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.deliveryapp.databinding.ActivityRiderDeliveryBinding
import com.android.deliveryapp.rider.DeliveryMapActivity
import com.android.deliveryapp.util.Keys.Companion.delivery
import com.android.deliveryapp.util.Keys.Companion.riders
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RiderDeliveryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRiderDeliveryBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRiderDeliveryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val user = auth.currentUser

        val date = intent.getStringExtra("date")
        val location = intent.getStringExtra("location")

        if (user != null) {
            getTotalPrice(firestore, user.email!!, date!!, location!!)
        }

        binding.deliveryMap.setOnClickListener {
            val intent = Intent(this@RiderDeliveryActivity, DeliveryMapActivity::class.java)
            intent.putExtra("clientLocation", location)
            startActivity(intent)
        }
        binding.riderChatClientBtn.setOnClickListener {
            // TODO: 28/02/2021
        }
        binding.riderChatManagerBtn.setOnClickListener {
            // TODO: 28/02/2021
        }
    }

    private fun getTotalPrice(firestore: FirebaseFirestore, email: String, date: String, location: String) {
        firestore.collection(riders).document(email)
            .collection(delivery).document(date)
            .get()
            .addOnSuccessListener { result ->
                binding.deliveryTotalPrice.text = getString(
                    R.string.total_price_delivery,
                    String.format("%.2f", result.getDouble("total") as Double)
                )
                binding.dateDelivery.text = getString(R.string.delivery_date, date)
                binding.deliveryPaymentType.text = getString(
                    R.string.delivery_payment_type,
                    result.getString("payment")
                )
                binding.locationDelivery.text = getString(R.string.delivery_location, location)
            }
            .addOnFailureListener { e ->
                Log.w("FIREBASE_FIRESTORE", "Error getting data", e)
            }
    }
}