package com.android.deliveryapp.rider

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.deliveryapp.R
import com.android.deliveryapp.databinding.ActivityRiderDeliveryBinding
import com.android.deliveryapp.util.Keys.Companion.MANAGER
import com.android.deliveryapp.util.Keys.Companion.delivery
import com.android.deliveryapp.util.Keys.Companion.riders
import com.android.deliveryapp.util.Keys.Companion.userType
import com.android.deliveryapp.util.Keys.Companion.users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RiderDeliveryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRiderDeliveryBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var clientEmail: String
    private lateinit var managerEmail: String

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
            getData(firestore, user.email!!, date!!, location!!)


            binding.deliveryMap.setOnClickListener {
                val intent = Intent(
                    this@RiderDeliveryActivity,
                    DeliveryMapActivity::class.java
                )
                intent.putExtra("clientLocation", location)
                startActivity(intent)
            }
            binding.riderChatClientBtn.setOnClickListener {
                val intent = Intent(
                    this@RiderDeliveryActivity,
                    RiderChatActivity::class.java
                )
                intent.putExtra("recipientEmail", clientEmail)
                intent.putExtra("riderEmail", user.email)
                startActivity(intent)
            }
            binding.riderChatManagerBtn.setOnClickListener {
                val intent = Intent(
                    this@RiderDeliveryActivity,
                    RiderChatActivity::class.java
                )
                intent.putExtra("riderEmail", user.email)

                getManagerEmail(firestore)
                intent.putExtra("recipientEmail", managerEmail)

                startActivity(intent)
            }
        }
    }

    override fun onStart() {
        super.onStart()

        val date = intent.getStringExtra("date")

        // finally remove from rider orders
        firestore.collection(riders).document(auth.currentUser?.email!!)
            .collection(delivery).document(date!!)
            .delete()
            .addOnSuccessListener {
                Log.d("FIREBASE_FIRESTORE", "Document deleted with success")
            }
            .addOnFailureListener { e ->
                Log.w("FIREBASE_FIRESTORE", "Failed to delete document", e)
            }
    }

    private fun getManagerEmail(firestore: FirebaseFirestore) {
        firestore.collection(users)
            .get()
            .addOnSuccessListener { result ->
                for (document in result.documents) {
                    if (document.getString(userType) as String == MANAGER) {
                        managerEmail = document.getString(userType) as String
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.w("ERROR", "Error getting manager email", e)
            }
    }

    private fun getData(firestore: FirebaseFirestore, email: String, date: String, location: String) {
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
                clientEmail = result.getString("clientEmail") as String
            }
            .addOnFailureListener { e ->
                Log.w("FIREBASE_FIRESTORE", "Error getting data", e)
            }
    }
}