package com.android.deliveryapp.manager

import android.os.Bundle
import android.util.Log
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

        // listen for new orders and get client email

        firestore.collection(orders).get()
            .addOnSuccessListener { result ->
                for (document in result.documents) {
                    // key = client.email, value = order date
                    emailList = emailList.plus(document.id to document.getString("date") as String)
                }
                getOrderDetails(firestore, emailList)
            }
            .addOnFailureListener { e ->
                Log.w("FIREBASE_FIRESTORE", "Error getting data", e)
            }
    }

    private fun getOrderDetails(firestore: FirebaseFirestore, emailList: Map<String, String>) {
        orderList = emptyArray()

        var products: Map<String, Any?> = emptyMap()

        for (email in emailList) {
            firestore.collection(clients).document(email.key)
                .collection(orders).document(email.value)
                .get()
                .addOnSuccessListener { result ->

                    var date = ""
                    var payment = ""
                    var total = 0.00

                    var price = 0.00
                    var quantity = 0
                    var title = ""

                    for (field in result.data as Map<String, Any?>) {
                        when (field.key) {
                            "date" -> date = field.value as String
                            "payment" -> payment = field.value as String
                            "products" -> {
                                for (item in field.value as Map<String, Any?>) {
                                    when (item.key) {
                                        "price" -> price = item.value as Double
                                        "quantity" -> quantity = item.value as Int
                                        "title" -> title = item.value as String
                                    }
                                    products = mapOf(
                                        "price" to price,
                                        "quantity" to quantity,
                                        "title" to title
                                    )
                                }
                            }
                            "total" -> total = field.value as Double
                        }
                    }
                    orderList = orderList.plus(ManagerOrderItem(email.key, date, total, payment))
                }
                .addOnFailureListener { e ->
                    Log.w("FIREBASE_FIRESTORE", "Error getting data", e)
                    Toast.makeText(baseContext,
                            getString(R.string.failure_data),
                            Toast.LENGTH_LONG).show()
                }
        }
        binding.ordersList.adapter = ManagerOrdersArrayAdapter(this,
                R.layout.manager_order_list_element,
                orderList)
    }
}