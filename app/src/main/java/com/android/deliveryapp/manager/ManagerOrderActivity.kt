package com.android.deliveryapp.manager

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.deliveryapp.R
import com.android.deliveryapp.databinding.ActivityManagerOrderBinding
import com.android.deliveryapp.manager.adapters.ManagerOrdersArrayAdapter
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

        // get new orders and get client orderEmail

        firestore.collection(orders).get()
            .addOnSuccessListener { result ->
                for (document in result.documents) {
                    for (data in document.data as Map<*, *>) {
                        // fetch orderEmail with date of the order
                        emailList = emailList.plus(data.key as String to data.value as String)
                    }
                }
                getOrder(firestore, emailList)
            }
                .addOnCanceledListener {  }
            .addOnFailureListener { e ->
                Log.w("FIREBASE_FIRESTORE", "Error getting data", e)
                Toast.makeText(baseContext,
                    getString(R.string.failure_data),
                    Toast.LENGTH_LONG).show()
            }

        binding.ordersList.setOnItemClickListener { _, view, _, _ ->

            val clientEmail: String = view.findViewById<TextView>(R.id.orderEmail).text.toString()
            val date = emailList[clientEmail]

            val intent = Intent(this@ManagerOrderActivity, ManagerOrderDetailActivity::class.java)
            intent.putExtra("clientEmail", clientEmail)
            intent.putExtra("orderDate", date)
            startActivity(intent)
        }
    }

    private fun getOrder(firestore: FirebaseFirestore, emailList: Map<String, String>) {
        for (data in emailList) {
            firestore.collection(clients).document(data.key)
                .collection(orders).document(data.value)
                .get()
                .addOnSuccessListener { result ->
                    var date = ""
                    var payment = ""
                    var total = 0.00

                    for (field in result.data as Map<String, Any?>) {
                        when (field.key) {
                            "date" -> date = field.value as String
                            "payment" -> payment = field.value as String
                            "total" -> total = field.value as Double
                        }
                    }
                    orderList = orderList.plus(ManagerOrderItem(data.key, date, total, payment))

                    updateView()
                }
                .addOnFailureListener { e ->
                    Log.w("FIREBASE_FIRESTORE", "Error getting data", e)
                    Toast.makeText(baseContext,
                            getString(R.string.failure_data),
                            Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun updateView() {
        if (orderList.isNotEmpty()) {
            binding.ordersList.visibility = View.VISIBLE
            binding.emptyOrdersLabel.visibility = View.INVISIBLE

            binding.ordersList.adapter = ManagerOrdersArrayAdapter(this,
                    R.layout.manager_order_list_element,
                    orderList)

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