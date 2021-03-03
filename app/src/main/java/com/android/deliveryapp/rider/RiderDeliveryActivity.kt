package com.android.deliveryapp.rider

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.deliveryapp.R
import com.android.deliveryapp.databinding.ActivityRiderDeliveryBinding
import com.android.deliveryapp.util.Keys.Companion.ACCEPTED
import com.android.deliveryapp.util.Keys.Companion.DELIVERED
import com.android.deliveryapp.util.Keys.Companion.DELIVERY_FAILED
import com.android.deliveryapp.util.Keys.Companion.MANAGER
import com.android.deliveryapp.util.Keys.Companion.REJECTED
import com.android.deliveryapp.util.Keys.Companion.chatCollection
import com.android.deliveryapp.util.Keys.Companion.delivery
import com.android.deliveryapp.util.Keys.Companion.deliveryHistory
import com.android.deliveryapp.util.Keys.Companion.orders
import com.android.deliveryapp.util.Keys.Companion.riders
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RiderDeliveryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRiderDeliveryBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var clientEmail: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRiderDeliveryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val user = auth.currentUser

        // show a back arrow button in actionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (user != null) {
            var date = ""
            var location = ""

            firestore.collection(riders).document(user.email!!)
                .collection(deliveryHistory)
                .get()
                .addOnSuccessListener { result ->
                    for (document in result.documents) {
                        // update view with outcome
                        date = document.getString("date") as String
                        location = document.getString("location") as String
                        updateView(document.getString("outcome") as String)
                    }
                    getData(firestore, user.email!!, date, location)
                }
                .addOnFailureListener { e ->
                    Log.w("FIREBASE_FIRESTORE", "Failed to get data", e)
                }

            binding.startDeliveryBtn.setOnClickListener {
                binding.deliveryMap.visibility = View.VISIBLE
                binding.riderChatManagerBtn.visibility = View.VISIBLE
                binding.riderChatClientBtn.visibility = View.VISIBLE

                binding.endDeliverySuccessBtn.visibility = View.VISIBLE
                binding.endDeliveryFailureBtn.visibility = View.VISIBLE
                binding.startDeliveryBtn.visibility = View.INVISIBLE

                sendMessageToClient(user.email!!, clientEmail)
            }
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

                intent.putExtra("recipientEmail", MANAGER)

                startActivity(intent)
            }
            binding.endDeliverySuccessBtn.setOnClickListener {
                updateView(DELIVERED)

                uploadData(firestore, date, user.email!!, DELIVERED)

                removeChat(firestore, user.email!!, clientEmail)

                startActivity(Intent(this@RiderDeliveryActivity,
                        RiderDeliveryHistoryActivity::class.java))

            }
            binding.endDeliveryFailureBtn.setOnClickListener {
                updateView(DELIVERY_FAILED)

                uploadData(firestore, date, user.email!!, DELIVERY_FAILED)

                removeChat(firestore, user.email!!, clientEmail)

                startActivity(Intent(this@RiderDeliveryActivity,
                        RiderDeliveryHistoryActivity::class.java))
            }
        }
    }

    private fun removeChat(firestore: FirebaseFirestore, riderEmail: String, clientEmail: String) {
        firestore.collection(chatCollection)
                .get()
                .addOnSuccessListener { result ->
                    for (document in result.documents) {
                        if (document.id == "$riderEmail|$clientEmail") {
                            document.reference.delete()
                            Log.d("FIREBASE_FIRESTORE", "Document deleted with success")
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("FIREBASE_FIRESTORE", "Error deleting document", e)
                }
    }

    private fun updateView(outcome: String) {
        when (outcome) {
            ACCEPTED -> {
                binding.riderChatManagerBtn.visibility = View.VISIBLE
                binding.startDeliveryBtn.visibility = View.VISIBLE
                binding.deliveryMap.visibility = View.VISIBLE
                binding.riderChatClientBtn.visibility = View.INVISIBLE
                binding.endDeliverySuccessBtn.visibility = View.INVISIBLE
                binding.endDeliveryFailureBtn.visibility = View.INVISIBLE
            }
            REJECTED -> {
                binding.riderChatManagerBtn.visibility = View.INVISIBLE
                binding.startDeliveryBtn.visibility = View.INVISIBLE
                binding.deliveryMap.visibility = View.VISIBLE
                binding.riderChatClientBtn.visibility = View.INVISIBLE
                binding.endDeliverySuccessBtn.visibility = View.INVISIBLE
                binding.endDeliveryFailureBtn.visibility = View.INVISIBLE
            }
            DELIVERED -> {
                binding.riderChatManagerBtn.visibility = View.VISIBLE
                binding.startDeliveryBtn.visibility = View.INVISIBLE
                binding.deliveryMap.visibility = View.INVISIBLE
                binding.riderChatClientBtn.visibility = View.INVISIBLE
                binding.endDeliverySuccessBtn.visibility = View.INVISIBLE
                binding.endDeliveryFailureBtn.visibility = View.INVISIBLE
            }
            DELIVERY_FAILED -> {
                binding.riderChatManagerBtn.visibility = View.VISIBLE
                binding.startDeliveryBtn.visibility = View.INVISIBLE
                binding.deliveryMap.visibility = View.VISIBLE
                binding.riderChatClientBtn.visibility = View.VISIBLE
                binding.endDeliverySuccessBtn.visibility = View.INVISIBLE
                binding.endDeliveryFailureBtn.visibility = View.INVISIBLE
            }
        }
    }

    private fun uploadData(firestore: FirebaseFirestore, date: String, riderEmail: String, outcome: String) {
        firestore.collection(riders).document(riderEmail)
                .collection(deliveryHistory).document(date)
                .update("outcome", outcome)
                .addOnSuccessListener {
                    // update also in orders
                    firestore.collection(orders).document(date)
                        .update("outcome", outcome)
                        .addOnSuccessListener {
                            Log.d("FIREBASE_FIRESTORE", "Data updated with success")

                            // delete entry in rider.email/delivery
                            firestore.collection(riders).document(riderEmail)
                                    .collection(delivery).document(date)
                                .delete()
                                .addOnSuccessListener {
                                    Log.d(
                                        "FIREBASE_FIRESTORE",
                                        "Document deleted with success"
                                    )

                                    Toast.makeText(baseContext,
                                        getString(R.string.data_update_success),
                                        Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    Log.w(
                                        "FIREBASE_FIRESTORE",
                                        "Failed to update data",
                                        e
                                    )

                                    Toast.makeText(baseContext,
                                        getString(R.string.error_updating_database),
                                        Toast.LENGTH_LONG).show()
                                }
                        }
                        .addOnFailureListener { e ->
                            Log.w("FIREBASE_FIRESTORE", "Failed to update data", e)

                            Toast.makeText(baseContext,
                                getString(R.string.error_updating_database),
                                Toast.LENGTH_LONG).show()
                        }
                }
                .addOnFailureListener { e ->
                    Log.w("FIREBASE_FIRESTORE", "Failed to update data", e)
                    Toast.makeText(baseContext,
                            getString(R.string.error_updating_database),
                            Toast.LENGTH_LONG).show()
                }
    }

    private fun sendMessageToClient(riderEmail: String, clientEmail: String) {
        val reference = FirebaseFirestore.getInstance().collection(chatCollection)
                .document("$riderEmail|$clientEmail")

        val automaticMessage = mapOf(
                "NAME" to "Rider",
                "TEXT" to getString(R.string.delivery_start_auto_msg)
        )

        reference.set(automaticMessage)
                .addOnSuccessListener {
                    Log.d("FIRESTORE_CHAT", "Message sent")
                }
                .addOnFailureListener { e ->
                    Log.e("ERROR", e.message.toString())
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