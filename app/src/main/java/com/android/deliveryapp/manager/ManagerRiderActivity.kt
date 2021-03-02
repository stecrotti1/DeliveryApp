package com.android.deliveryapp.manager

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.deliveryapp.R
import com.android.deliveryapp.databinding.ActivityManagerRiderBinding
import com.android.deliveryapp.util.Keys
import com.android.deliveryapp.util.Keys.Companion.ACCEPTED
import com.android.deliveryapp.util.Keys.Companion.REJECTED
import com.android.deliveryapp.util.Keys.Companion.deliveryHistory
import com.android.deliveryapp.util.Keys.Companion.riders
import com.android.deliveryapp.util.RiderProductItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import java.text.DateFormat
import java.util.*

class ManagerRiderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManagerRiderBinding
    private lateinit var firestore: FirebaseFirestore

    private val channelID = "1"
    private val notificationID = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManagerRiderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        val riderEmail = intent.getStringExtra("riderEmail")
        val clientEmail = intent.getStringExtra("clientEmail")
        val orderDate = intent.getStringExtra("orderDate")

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // reload this activity
        val refresh = Intent(this@ManagerRiderActivity, ManagerRiderActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(this@ManagerRiderActivity,
            0,
            refresh,
            0)

        binding.riderInfo.text = getString(R.string.rider_selected_title, riderEmail)

        binding.chatWithRiderBtn.visibility = View.INVISIBLE
        binding.selectBtn.visibility = View.VISIBLE
        binding.riderInfo.visibility = View.INVISIBLE

        // if driver has accepted then show the chat button
        firestore.collection(riders).document(riderEmail!!)
            .collection(deliveryHistory)
            .get()
            .addOnSuccessListener { result ->
                for (document in result.documents) {
                    if (document.getString("outcome") as String == ACCEPTED) {
                        binding.chatWithRiderBtn.visibility = View.VISIBLE
                        binding.selectBtn.visibility = View.INVISIBLE

                        binding.riderInfo.text = getString(R.string.rider_info_msg,
                            getString(R.string.rider_accept))
                        binding.riderInfo.visibility = View.VISIBLE

                        return@addOnSuccessListener
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.w("FIREBASE_FIRESTORE", "Error getting data", e)
            }

        binding.chatWithRiderBtn.setOnClickListener {
            val intent = Intent(this@ManagerRiderActivity, ManagerChatActivity::class.java)
            intent.putExtra("riderEmail", riderEmail)

            startActivity(intent)
        }

        binding.selectBtn.setOnClickListener {
            if (clientEmail != null && orderDate != null) {
                sendOrderToRider(firestore, riderEmail, clientEmail, orderDate)
            }
        }

        // listen for rider response
        firestore.collection(riders).document(riderEmail)
            .collection(deliveryHistory).document(orderDate!!)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.w("FIREBASE_FIRESTORE", "Listen failed", error)
                    return@addSnapshotListener
                } else { // FIXME: 02/03/2021
                    if (value?.getString("outcome") as String == ACCEPTED) {
                        binding.riderInfo.text = getString(
                            R.string.rider_info_msg,
                            getString(R.string.rider_accept)
                        )
                        binding.riderInfo.visibility = View.VISIBLE
                    }
                    else if (value.getString("outcome") as String == REJECTED) {
                        binding.riderInfo.text = getString(
                            R.string.rider_info_msg,
                            getString(R.string.rider_reject)
                        )
                        binding.riderInfo.visibility = View.VISIBLE
                    }
                    // notification
                    createNotification(pendingIntent, notificationManager, binding.riderInfo.text.toString())
                    createNotificationChannel(
                        channelID,
                        getString(R.string.app_name),
                        getString(R.string.new_delivery_title),
                        notificationManager
                    )
                }
            }
    }

    private fun createNotification(pendingIntent: PendingIntent,
                                   notificationManager: NotificationManager,
                                   text: String) {
        val notification = Notification.Builder(this@ManagerRiderActivity, channelID)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle(getString(R.string.rider_has_responded))
            .setContentText(text)
            .setAutoCancel(true)
            .setChannelId(channelID)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationID, notification)
    }

    private fun createNotificationChannel(id : String, name: String, description: String, notificationManager: NotificationManager) {
        val priority = NotificationManager.IMPORTANCE_HIGH

        val channel = NotificationChannel(id, name, priority)

        channel.description = description

        notificationManager.createNotificationChannel(channel)
    }

    private fun sendOrderToRider(firestore: FirebaseFirestore, rider: String, clientEmail: String, clientOrderDate: String) {
        val today = getDate()

        var entry: Map<String, Any?>

        // GET CLIENT ADDRESS
        firestore.collection(Keys.clients).document(clientEmail)
            .get()
            .addOnSuccessListener { result ->
                // GET PRODUCTS
                firestore.collection(Keys.clients).document(clientEmail)
                    .collection(Keys.orders).document(clientOrderDate)
                    .get()
                    .addOnSuccessListener { result2 ->
                        var productList: List<RiderProductItem> = emptyList()

                        var price = 0.00
                        var quantity: Long = 0
                        var title = ""

                        val paymentType: String = result2.getString("payment") as String

                        for (field in result2.get("products") as ArrayList<Map<String, Any?>>) {
                            for (item in field) {
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
                            "clientEmail" to clientEmail
                        )

                        // send cliend position, total price and products
                        firestore.collection(Keys.riders).document(rider)
                            .collection(Keys.delivery).document(today)
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

    private fun getDate(): String {
        val today: Date = Calendar.getInstance().time

        return DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM).format(today)
    }
}