package com.android.deliveryapp.rider

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.android.deliveryapp.R
import com.android.deliveryapp.databinding.ActivityRiderDeliveryBinding
import com.android.deliveryapp.util.Keys.Companion.ACCEPTED
import com.android.deliveryapp.util.Keys.Companion.MANAGER
import com.android.deliveryapp.util.Keys.Companion.chatCollection
import com.android.deliveryapp.util.Keys.Companion.delivery
import com.android.deliveryapp.util.Keys.Companion.deliveryHistory
import com.android.deliveryapp.util.Keys.Companion.newDelivery
import com.android.deliveryapp.util.Keys.Companion.riders
import com.android.deliveryapp.util.Keys.Companion.userInfo
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

        if (user != null) {
            var date = ""
            var location = ""

            firestore.collection(riders).document(user.email!!)
                .collection(deliveryHistory)
                .get()
                .addOnSuccessListener { result ->


                    for (document in result.documents) {
                        // if it is accepted then it is the current delivery
                        if (document.getString("outcome") as String == ACCEPTED) {
                            date = document.getString("date") as String
                            location = document.getString("location") as String
                        }
                    }
                    getData(firestore, user.email!!, date, location)
                }
                .addOnFailureListener { e ->
                    Log.w("FIREBASE_FIRESTORE", "Failed to get data", e)
                }

            val sharedPreferences = getSharedPreferences(userInfo, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()

            if (sharedPreferences.getBoolean(newDelivery, false)) { // delivery hasn't begun yet
                binding.deliveryMap.visibility = View.INVISIBLE
                binding.riderChatManagerBtn.visibility = View.INVISIBLE
                binding.riderChatClientBtn.visibility = View.INVISIBLE

                binding.endDeliverySuccessBtn.visibility = View.VISIBLE
                binding.endDeliveryFailureBtn.visibility = View.VISIBLE
                binding.startDeliveryBtn.visibility = View.VISIBLE
            } else {
                binding.deliveryMap.visibility = View.VISIBLE
                binding.riderChatManagerBtn.visibility = View.VISIBLE
                binding.riderChatClientBtn.visibility = View.VISIBLE

                binding.endDeliverySuccessBtn.visibility = View.INVISIBLE
                binding.endDeliveryFailureBtn.visibility = View.INVISIBLE
                binding.startDeliveryBtn.visibility = View.INVISIBLE
            }

            binding.startDeliveryBtn.setOnClickListener {
                editor.putBoolean(newDelivery, false)
                editor.apply()

                binding.deliveryMap.visibility = View.VISIBLE
                binding.riderChatManagerBtn.visibility = View.VISIBLE
                binding.riderChatClientBtn.visibility = View.VISIBLE

                binding.endDeliverySuccessBtn.visibility = View.INVISIBLE
                binding.endDeliveryFailureBtn.visibility = View.INVISIBLE
                binding.startDeliveryBtn.visibility = View.INVISIBLE

                // finally remove from rider orders
                firestore.collection(riders).document(auth.currentUser?.email!!)
                        .collection(delivery).document(date)
                        .delete()
                        .addOnSuccessListener {
                            Log.d("FIREBASE_FIRESTORE", "Document deleted with success")
                        }
                        .addOnFailureListener { e ->
                            Log.w("FIREBASE_FIRESTORE", "Failed to delete document", e)
                        }

                sendNotificationToClient(user.email!!, clientEmail)
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

                getManagerEmail(firestore)
                intent.putExtra("recipientEmail", managerEmail)

                startActivity(intent)
            }
        }
    }

    private fun sendNotificationToClient(riderEmail: String, clientEmail: String) {
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