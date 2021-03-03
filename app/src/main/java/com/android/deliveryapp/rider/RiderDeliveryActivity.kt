package com.android.deliveryapp.rider

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.deliveryapp.R
import com.android.deliveryapp.databinding.ActivityRiderDeliveryBinding
import com.android.deliveryapp.util.Keys.Companion.ACCEPTED
import com.android.deliveryapp.util.Keys.Companion.DELIVERED
import com.android.deliveryapp.util.Keys.Companion.DELIVERY_FAILED
import com.android.deliveryapp.util.Keys.Companion.MANAGER
import com.android.deliveryapp.util.Keys.Companion.REJECTED
import com.android.deliveryapp.util.Keys.Companion.START
import com.android.deliveryapp.util.Keys.Companion.chatCollection
import com.android.deliveryapp.util.Keys.Companion.delivery
import com.android.deliveryapp.util.Keys.Companion.deliveryHistory
import com.android.deliveryapp.util.Keys.Companion.newDelivery
import com.android.deliveryapp.util.Keys.Companion.orders
import com.android.deliveryapp.util.Keys.Companion.riderStatus
import com.android.deliveryapp.util.Keys.Companion.riders
import com.android.deliveryapp.util.Keys.Companion.userInfo
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint

class RiderDeliveryActivity : AppCompatActivity() {
    // TODO: 03/03/2021 chat notifications 
    private lateinit var binding: ActivityRiderDeliveryBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var clientEmail: String

    private val LOCATION_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRiderDeliveryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val user = auth.currentUser

        // show a back arrow button in actionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        var fusedLocation: FusedLocationProviderClient

        val sharedPreferences = getSharedPreferences(userInfo, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        if (user != null) {
            var date = ""
            var location = ""

            // get outcomes
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
                    getData(firestore, date, location)
                }
                .addOnFailureListener { e ->
                    Log.w("FIREBASE_FIRESTORE", "Failed to get data", e)
                }

            /************************ START DELIVERY ***********************************/

            binding.startDeliveryBtn.setOnClickListener {
                updateView(START)

                uploadData(firestore, date, user.email!!, START)

                sendMessageToClient(user.email!!, clientEmail)
            }

            /**************************** SHARE LOCATION ******************************/
            // FIXME: 03/03/2021 not sharing
            binding.shareLocationBtn.setOnClickListener {
                val permission = ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )

                if (permission != PackageManager.PERMISSION_GRANTED) {
                    requestPermission(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        LOCATION_REQUEST_CODE
                    )
                } else {
                    fusedLocation = LocationServices.getFusedLocationProviderClient(baseContext)

                    fusedLocation.lastLocation
                        .addOnSuccessListener { location: Location? ->
                            if (location != null) {
                                updateLocation(firestore, location, user.email!!)
                            }
                        }
                }
            }

            /************************** VIEW MAP ***********************************/

            binding.deliveryMap.setOnClickListener {
                val intent = Intent(
                    this@RiderDeliveryActivity,
                    DeliveryMapActivity::class.java
                )
                intent.putExtra("clientLocation", location)
                startActivity(intent)
            }

            /********************** CHAT WITH CLIENT ******************************/

            binding.riderChatClientBtn.setOnClickListener {
                val intent = Intent(
                    this@RiderDeliveryActivity,
                    RiderChatActivity::class.java
                )
                intent.putExtra("recipientEmail", clientEmail)
                intent.putExtra("riderEmail", user.email)
                startActivity(intent)
            }

            /********************** CHAT WITH MANAGER ***************************/

            binding.riderChatManagerBtn.setOnClickListener {
                val intent = Intent(
                    this@RiderDeliveryActivity,
                    RiderChatActivity::class.java
                )
                intent.putExtra("riderEmail", user.email)

                intent.putExtra("recipientEmail", MANAGER)

                startActivity(intent)
            }

            /********************* END DELIVERY SUCCESS **********************/

            binding.endDeliverySuccessBtn.setOnClickListener {
                updateView(DELIVERED)

                uploadData(firestore, date, user.email!!, DELIVERED)

                removeChat(firestore, user.email!!, clientEmail)

                editor.putBoolean(newDelivery, false)
                editor.apply()

                startActivity(Intent(this@RiderDeliveryActivity,
                        RiderDeliveryHistoryActivity::class.java))

            }

            /************************ END DELIVERY FAILURE ***********************/

            binding.endDeliveryFailureBtn.setOnClickListener {
                updateView(DELIVERY_FAILED)

                uploadData(firestore, date, user.email!!, DELIVERY_FAILED)

                editor.putBoolean(newDelivery, false)
                editor.apply()

                removeChat(firestore, user.email!!, clientEmail)

                startActivity(Intent(this@RiderDeliveryActivity,
                        RiderDeliveryHistoryActivity::class.java))
            }
        }
    }

    private fun updateLocation(firestore: FirebaseFirestore, location: Location, email: String) {
        val sharedPreferences = getSharedPreferences(userInfo, Context.MODE_PRIVATE)


        val entry = mapOf(
            "riderPosition" to GeoPoint(location.latitude, location.longitude),
            riderStatus to sharedPreferences.getBoolean(riderStatus, false)
        )

        firestore.collection(riders).document(email)
            .set(entry)
            .addOnSuccessListener {
                Log.d("FIREBASE_FIRESTORE", "Location updated with success")

                Toast.makeText(
                    baseContext,
                    getString(R.string.location_update_success),
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                Log.w("FIREBASE_FIRESTORE", "Error updating position", e)

                Toast.makeText(
                    baseContext,
                    getString(R.string.location_update_failure),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    /**
     * Request current location permission
     */
    private fun requestPermission(permissionType: String, requestCode: Int) {
        ActivityCompat.requestPermissions(this, arrayOf(permissionType), requestCode)
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
                binding.shareLocationBtn.visibility = View.INVISIBLE
                binding.riderChatClientBtn.visibility = View.INVISIBLE
                binding.endDeliverySuccessBtn.visibility = View.INVISIBLE
                binding.endDeliveryFailureBtn.visibility = View.INVISIBLE
            }
            START -> {
                binding.riderChatManagerBtn.visibility = View.VISIBLE
                binding.startDeliveryBtn.visibility = View.INVISIBLE
                binding.deliveryMap.visibility = View.VISIBLE
                binding.shareLocationBtn.visibility = View.VISIBLE
                binding.riderChatClientBtn.visibility = View.VISIBLE
                binding.endDeliverySuccessBtn.visibility = View.VISIBLE
                binding.endDeliveryFailureBtn.visibility = View.VISIBLE
            }
            REJECTED -> {
                binding.riderChatManagerBtn.visibility = View.INVISIBLE
                binding.startDeliveryBtn.visibility = View.INVISIBLE
                binding.deliveryMap.visibility = View.VISIBLE
                binding.shareLocationBtn.visibility = View.INVISIBLE
                binding.riderChatClientBtn.visibility = View.INVISIBLE
                binding.endDeliverySuccessBtn.visibility = View.INVISIBLE
                binding.endDeliveryFailureBtn.visibility = View.INVISIBLE
            }
            DELIVERED -> {
                binding.riderChatManagerBtn.visibility = View.VISIBLE
                binding.startDeliveryBtn.visibility = View.INVISIBLE
                binding.deliveryMap.visibility = View.INVISIBLE
                binding.shareLocationBtn.visibility = View.INVISIBLE
                binding.riderChatClientBtn.visibility = View.INVISIBLE
                binding.endDeliverySuccessBtn.visibility = View.INVISIBLE
                binding.endDeliveryFailureBtn.visibility = View.INVISIBLE
            }
            DELIVERY_FAILED -> {
                binding.riderChatManagerBtn.visibility = View.VISIBLE
                binding.startDeliveryBtn.visibility = View.INVISIBLE
                binding.deliveryMap.visibility = View.VISIBLE
                binding.shareLocationBtn.visibility = View.INVISIBLE
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
                            if (outcome != START && outcome != ACCEPTED) {
                                // delete entry in rider.email/delivery
                                firestore.collection(riders).document(riderEmail)
                                    .collection(delivery).document(date)
                                    .delete()
                                    .addOnSuccessListener {
                                        Log.d(
                                            "FIREBASE_FIRESTORE",
                                            "Document deleted with success"
                                        )

                                        Toast.makeText(
                                            baseContext,
                                            getString(R.string.data_update_success),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w(
                                            "FIREBASE_FIRESTORE",
                                            "Failed to update data",
                                            e
                                        )

                                        Toast.makeText(
                                            baseContext,
                                            getString(R.string.error_updating_database),
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.w("FIREBASE_FIRESTORE", "Failed to update data", e)

                            Toast.makeText(
                                baseContext,
                                getString(R.string.error_updating_database),
                                Toast.LENGTH_LONG
                            ).show()
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

    private fun getData(firestore: FirebaseFirestore, date: String, location: String) {
        firestore.collection(orders).document(date)
            .get()
            .addOnSuccessListener { result ->
                binding.deliveryTotalPrice.text = getString(
                    R.string.total_price_delivery,
                    String.format("%.2f €", result.getDouble("total") as Double)
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