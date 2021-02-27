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
import com.android.deliveryapp.util.Keys
import com.android.deliveryapp.util.Keys.Companion.clients
import com.android.deliveryapp.util.Keys.Companion.delivery
import com.android.deliveryapp.util.Keys.Companion.riderStatus
import com.android.deliveryapp.util.Keys.Companion.riders
import com.android.deliveryapp.util.RiderListItem
import com.android.deliveryapp.util.RiderProductItem
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import java.text.DateFormat
import java.util.*

/**
 * Activity used by MANAGER
 */
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

    override fun onStart() {
        super.onStart()

        binding.ridersList.setOnItemClickListener { _, _, i, _ ->
            if (!riderList[i].availability) { // if rider is not available, show an alert dialog
                showUnavailabilityDialog()
            } else {
                val email = intent.getStringExtra("clientEmail")
                val clientOrderDate = intent.getStringExtra("orderDate")

                if (email != null && clientOrderDate != null) {

                    sendOrderToRider(firestore, riderList[i].email, email, clientOrderDate)
                }
            }
        }
        /*
        // listen for rider status change
        firestore.collection(riders)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        Log.w("FIREBASE_FIRESTORE", "Listen failed", error)
                        return@addSnapshotListener
                    } else {

                    }
                }

         */
    }

    private fun getTotalPrice(productList: List<RiderProductItem>): Double {
        var total = 0.0
        if (productList.isNotEmpty()) {
            for (item in productList) {
                total += item.price
            }
        }
        return total
    }

    private fun getDate(): String {
        val today: Date = Calendar.getInstance().time

        return DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM).format(today)
    }

    private fun sendOrderToRider(firestore: FirebaseFirestore, rider: String, email: String, clientOrderDate: String) {
        val today = getDate()

        var entry: Map<String, Any?>

        // GET CLIENT ADDRESS
        firestore.collection(clients).document(email)
                .get()
                .addOnSuccessListener { result ->
                    // GET PRODUCTS
                    firestore.collection(clients).document(email)
                            .collection(Keys.orders).document(clientOrderDate)
                            .get()
                            .addOnSuccessListener { result2 ->
                                var productList: List<RiderProductItem> = emptyList()

                                var price = 0.00
                                var quantity: Long = 0
                                var title = ""

                                for (field in result2.get("products") as ArrayList<Map<String, Any?>>) {
                                    for (item in field) {
                                        when(item.key) {
                                            "price" -> price = item.value as Double
                                            "quantity" -> quantity = item.value as Long
                                            "title" -> title = item.value as String
                                        }
                                    }
                                    productList = productList.plus(RiderProductItem(title,
                                            quantity.toInt(),
                                            price))
                                }

                                entry = mapOf(
                                        "products" to productList,
                                        "total" to getTotalPrice(productList),
                                        "address" to result.getGeoPoint("address") as GeoPoint
                                )

                                // send cliend position, total price and products
                                firestore.collection(riders).document(rider)
                                        .collection(delivery).document(today)
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