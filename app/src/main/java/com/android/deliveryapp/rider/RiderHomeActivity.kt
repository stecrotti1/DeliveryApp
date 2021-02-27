package com.android.deliveryapp.rider

import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.deliveryapp.LoginActivity
import com.android.deliveryapp.R
import com.android.deliveryapp.databinding.ActivityRiderHomeBinding
import com.android.deliveryapp.rider.adapters.RiderOrdersArrayAdapter
import com.android.deliveryapp.util.Keys
import com.android.deliveryapp.util.Keys.Companion.clientAddress
import com.android.deliveryapp.util.Keys.Companion.delivery
import com.android.deliveryapp.util.Keys.Companion.riders
import com.android.deliveryapp.util.RiderOrderItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import java.io.IOException
import kotlin.math.*

class RiderHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRiderHomeBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var orders: Array<RiderOrderItem>
    private lateinit var marketPos: GeoPoint

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRiderHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val user = auth.currentUser

        if (user != null) {
            getOrders(firestore, user.email!!)
        }
    }

    override fun onStart() {
        super.onStart()

        binding.riderOrdersList.setOnItemClickListener { adapterView, view, i, l ->

        }
    }

    private fun showOrderDialog(i: Int) {
        val dialog: AlertDialog?
    }

    private fun getOrderDetail(date: String, firestore: FirebaseFirestore, email: String) {
        firestore.collection(riders).document(email)
                .collection(delivery).document(date)
                .get()
                .addOnSuccessListener { result ->

                }
    }

    private fun getOrders(firestore: FirebaseFirestore, email: String) {
        firestore.collection(riders).document(email)
                .collection(delivery)
                .get()
                .addOnSuccessListener { result ->
                    var date: String
                    var location = ""
                    var locationGeoPoint: GeoPoint
                    var distance: Double
                    var geocoder: List<Address>? = null

                    for (document in result.documents) {
                        date = document.id
                        locationGeoPoint = document.getGeoPoint(clientAddress) as GeoPoint

                       try {
                           geocoder = Geocoder(this).getFromLocation(locationGeoPoint.latitude,
                                   locationGeoPoint.longitude,
                                   1)
                       } catch (e: IOException) {
                           Log.w("Geocoder", e.message.toString())
                       }

                        if (geocoder != null) {
                            location = "${geocoder[0].getAddressLine(0)}, " +
                                    "${geocoder[0].getAddressLine(1)}, " +
                                    geocoder[0].postalCode
                        }

                        getMarketPosition(firestore)

                        distance = calculateDistanceFromMarket(marketPos, locationGeoPoint)

                        orders = orders.plus(RiderOrderItem(date, location, distance))
                    }
                    updateView()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(baseContext,
                            getString(R.string.error_user_data),
                            Toast.LENGTH_SHORT).show()

                    Log.w("FIREBASE_FIRESTORE",
                            "Error getting orders",
                            e)
                }
    }

    private fun updateView() {
        if (orders.isNotEmpty()) {
            binding.empty.visibility = View.INVISIBLE
            binding.riderOrdersList.visibility = View.VISIBLE

            binding.riderOrdersList.adapter = RiderOrdersArrayAdapter(this,
                    R.layout.rider_order_list_element,
                    orders)
        } else { // empty
            binding.empty.visibility = View.VISIBLE
            binding.riderOrdersList.visibility = View.INVISIBLE
        }
    }

    private fun getMarketPosition(firestore: FirebaseFirestore) {
        firestore.collection(Keys.marketPosFirestore)
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        marketPos = document.getGeoPoint(Keys.fieldPosition) as GeoPoint
                    }

                }
                .addOnFailureListener { exception ->
                    Log.w("Firestore", "Error getting documents", exception)
                }
    }

    private fun calculateDistanceFromMarket(market: GeoPoint, clientGeoPoint: GeoPoint): Double {
        val lon1: Double = Math.toRadians(market.longitude)
        val lat1: Double = Math.toRadians(market.latitude)

        val lon2: Double = Math.toRadians(clientGeoPoint.longitude)
        val lat2: Double = Math.toRadians(clientGeoPoint.latitude)

        val distanceLng: Double = lon2 - lon1
        val distanceLat: Double = lat2 - lat1

        val a: Double = sin(distanceLat / 2).pow(2.0) + cos(lat1) * cos(lat2) * sin(distanceLng / 2).pow(2.0)
        val c = 2 * asin(sqrt(a))

        return (6367 * c)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.rider_home_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        auth = FirebaseAuth.getInstance()
        return when (item.itemId) {
            R.id.riderProfile -> {
                startActivity(Intent(this@RiderHomeActivity, RiderProfileActivity::class.java))
                true
            }
            R.id.riderDeliveries -> {
                // TODO: 19/02/2021 history deliveries 
                true
            }
            R.id.logout -> {
                auth.signOut()
                startActivity(Intent(this@RiderHomeActivity, LoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}