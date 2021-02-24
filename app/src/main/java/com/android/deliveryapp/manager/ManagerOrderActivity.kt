package com.android.deliveryapp.manager

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.deliveryapp.R
import com.android.deliveryapp.databinding.ActivityManagerOrderBinding
import com.android.deliveryapp.util.Keys.Companion.clients
import com.android.deliveryapp.util.Keys.Companion.orders
import com.android.deliveryapp.util.ManagerOrderItem
import com.google.firebase.firestore.FirebaseFirestore

class ManagerOrderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManagerOrderBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var emailList: Map<String, String>
    private lateinit var orderList: Array<ManagerOrderItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManagerOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        emailList = emptyMap()
        orderList = emptyArray()

        // show a back arrow button in actionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // listen for new orders and get client email

        firestore.collection(orders).get()
            .addOnSuccessListener { result ->
                Toast.makeText(baseContext, "ee", Toast.LENGTH_SHORT).show()


                //getOrderDetails(firestore, emailList)
            }
            .addOnFailureListener { e ->
                Log.w("FIREBASE_FIRESTORE", "Error getting data", e)
                Toast.makeText(baseContext,
                    getString(R.string.failure_data),
                    Toast.LENGTH_LONG).show()
            }
    }

    private fun getOrderDetails(firestore: FirebaseFirestore, emailList: Map<String, String>) {
        var products: Map<String, Any?> = emptyMap()

        for (email in emailList) {
            firestore.collection(clients).document(email.key)
                .collection(orders).document(email.value)
                .get()
                .addOnSuccessListener { result ->
                    for (field in result.data as Map<String, Any?>) {
                    }
                }
                .addOnFailureListener {
                }
        }

        updateView()
    }

    private fun updateView() {
        if (orderList.isNotEmpty()) {
            binding.ordersList.visibility = View.VISIBLE
            binding.emptyOrdersLabel.visibility = View.INVISIBLE
        } else { // empty
            binding.ordersList.visibility = View.INVISIBLE
            binding.emptyOrdersLabel.visibility = View.VISIBLE
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