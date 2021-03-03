package com.android.deliveryapp.manager

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.deliveryapp.R
import com.android.deliveryapp.databinding.ActivityManagerRiderBinding
import com.android.deliveryapp.util.Keys.Companion.ACCEPTED
import com.android.deliveryapp.util.Keys.Companion.DELIVERED
import com.android.deliveryapp.util.Keys.Companion.DELIVERY_FAILED
import com.android.deliveryapp.util.Keys.Companion.REJECTED
import com.android.deliveryapp.util.Keys.Companion.START
import com.android.deliveryapp.util.Keys.Companion.YET_TO_RESPOND
import com.android.deliveryapp.util.Keys.Companion.clientEmail
import com.android.deliveryapp.util.Keys.Companion.clients
import com.android.deliveryapp.util.Keys.Companion.delivery
import com.android.deliveryapp.util.Keys.Companion.managerPref
import com.android.deliveryapp.util.Keys.Companion.orderDate
import com.android.deliveryapp.util.Keys.Companion.orders
import com.android.deliveryapp.util.Keys.Companion.riderEmail
import com.android.deliveryapp.util.Keys.Companion.riders
import com.android.deliveryapp.util.RiderProductItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import java.util.*

class ManagerRiderActivity : AppCompatActivity() {
    // TODO: 03/03/2021 chat notification
    private lateinit var binding: ActivityManagerRiderBinding
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManagerRiderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        val sharedPreferences = getSharedPreferences(managerPref, Context.MODE_PRIVATE)

        // show a back arrow button in actionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val riderEmail = sharedPreferences.getString(riderEmail, "")
        val clientEmail = sharedPreferences.getString(clientEmail, "")
        val orderDate = sharedPreferences.getString(orderDate, "")

        binding.riderSelected.text = getString(R.string.rider_selected_title, riderEmail)

        binding.chatWithRiderBtn.visibility = View.INVISIBLE
        binding.selectBtn.visibility = View.INVISIBLE
        binding.riderInfo.visibility = View.INVISIBLE
        binding.selectAnotherRiderBtn.visibility = View.INVISIBLE

        // update view
        firestore.collection(riders).document(riderEmail!!)
            .collection(delivery)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    for (document in result.documents) {
                        if (document.contains("outcome")) {
                            updateView(document.getString("outcome") as String)
                        }
                    }
                } else {
                    updateView("")
                }
            }
            .addOnFailureListener { e ->
                Log.w("FIREBASE_FIRESTORE", "Error getting data", e)
            }

        binding.chatWithRiderBtn.setOnClickListener {
            val intent = Intent(this@ManagerRiderActivity, ManagerChatActivity::class.java)

            startActivity(intent)
        }

        binding.selectBtn.setOnClickListener {
            if (clientEmail != null && orderDate != null) {
                sendOrderToRider(firestore, riderEmail!!, clientEmail, orderDate)
            }
        }

        binding.selectAnotherRiderBtn.setOnClickListener {
            startActivity(Intent(
                this@ManagerRiderActivity,
                ManagerRidersListActivity::class.java
            ))
            finish()
        }

    }

    private fun updateView(outcome: String) {
        when (outcome) {
            ACCEPTED, START -> {
                binding.chatWithRiderBtn.visibility = View.VISIBLE
                binding.selectBtn.visibility = View.INVISIBLE

                binding.riderInfo.text = getString(
                    R.string.rider_info_msg,
                    getString(R.string.rider_accept)
                )
                binding.riderInfo.visibility = View.VISIBLE
                binding.selectAnotherRiderBtn.visibility = View.INVISIBLE
            }
            REJECTED -> {
                binding.chatWithRiderBtn.visibility = View.INVISIBLE
                binding.selectBtn.visibility = View.INVISIBLE

                binding.riderInfo.text = getString(
                    R.string.rider_info_msg,
                    getString(R.string.rider_reject)
                )
                binding.riderInfo.visibility = View.VISIBLE
                binding.selectAnotherRiderBtn.visibility = View.VISIBLE
            }
            DELIVERED -> {
                binding.chatWithRiderBtn.visibility = View.VISIBLE
                binding.selectBtn.visibility = View.INVISIBLE

                binding.riderInfo.text = getString(
                    R.string.rider_info_msg,
                    getString(R.string.delivery_success)
                )
                binding.riderInfo.visibility = View.VISIBLE
                binding.selectAnotherRiderBtn.visibility = View.INVISIBLE
            }
            DELIVERY_FAILED -> {
                binding.chatWithRiderBtn.visibility = View.VISIBLE
                binding.selectBtn.visibility = View.INVISIBLE

                binding.riderInfo.text = getString(
                    R.string.rider_info_msg,
                    getString(R.string.delivery_failure)
                )
                binding.riderInfo.visibility = View.VISIBLE
                binding.selectAnotherRiderBtn.visibility = View.INVISIBLE
            }
            else -> {
                binding.chatWithRiderBtn.visibility = View.INVISIBLE
                binding.selectBtn.visibility = View.VISIBLE
                binding.riderInfo.visibility = View.INVISIBLE
                binding.selectAnotherRiderBtn.visibility = View.INVISIBLE
            }
        }
    }

    override fun onStart() {
        super.onStart()

        val sharedPreferences = getSharedPreferences(managerPref, Context.MODE_PRIVATE)
        // listen for rider response
        val orderDate = sharedPreferences.getString(orderDate, "")

        if (orderDate != null) {
            firestore.collection(orders).document(orderDate)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        Log.w("FIREBASE_FIRESTORE", "Listen failed", error)
                        return@addSnapshotListener
                    } else {
                        if (value != null && value.contains("outcome")) {
                            updateView(value.getString("outcome") as String)
                        }
                    }
                }
        }
    }

    private fun sendOrderToRider(firestore: FirebaseFirestore, rider: String, clientEmail: String, clientOrderDate: String) {
        var entry: Map<String, Any?>

        // GET CLIENT ADDRESS
        firestore.collection(clients).document(clientEmail)
            .get()
            .addOnSuccessListener { result ->
                // GET PRODUCTS
                firestore.collection(orders).document(clientOrderDate)
                    .get()
                    .addOnSuccessListener { result1 ->
                        var productList: List<RiderProductItem> = emptyList()

                        var price = 0.00
                        var quantity: Long = 0
                        var title = ""

                        val paymentType: String = result1.getString("payment") as String

                        for (field in result1.get("products") as ArrayList<*>) {
                            for (item in field as Map<*, *>) {
                                when(item.key) {
                                    "price" -> price = item.value as Double
                                    "quantity" -> quantity = item.value as Long
                                    "title" -> title = item.value as String
                                }
                            }
                            productList = productList.plus(
                                RiderProductItem(title,
                                quantity.toInt(),
                                price)
                            )
                        }

                        entry = mapOf(
                            "products" to productList,
                            "total" to getTotalPrice(productList),
                            "address" to result.getGeoPoint("address") as GeoPoint,
                            "payment" to paymentType,
                            "clientEmail" to clientEmail,
                            "outcome" to YET_TO_RESPOND
                        )

                        // send cliend position, total price and products
                        firestore.collection(riders).document(rider)
                            .collection(delivery).document(clientOrderDate)
                            .set(entry)
                            .addOnSuccessListener {
                                Toast.makeText(baseContext,
                                    getString(R.string.order_sent_success),
                                    Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(baseContext,
                                    getString(R.string.order_sent_failure),
                                    Toast.LENGTH_SHORT).show()

                                Log.w("FIREBASE_FIRESTORE",
                                    "Error sending order",
                                    e)
                            }

                    }
                    .addOnFailureListener { e ->
                        Log.w("FIREBASE_FIRESTORE", "Error getting data", e)
                        Toast.makeText(baseContext,
                            getString(R.string.failure_data),
                            Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { e ->
                Log.w("FIREBASE_FIRESTORE", "Error getting client address", e)

                Toast.makeText(baseContext,
                    getString(R.string.error_user_data),
                    Toast.LENGTH_LONG).show()
            }
    }

    private fun getTotalPrice(productList: List<RiderProductItem>): Double {
        var total = 0.0
        if (productList.isNotEmpty()) {
            for (item in productList) {
                total += (item.price * item.quantity)
            }
        }
        return total
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