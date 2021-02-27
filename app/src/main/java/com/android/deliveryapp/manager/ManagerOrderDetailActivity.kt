package com.android.deliveryapp.manager

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.deliveryapp.R
import com.android.deliveryapp.databinding.ActivityManagerOrderDetailBinding
import com.android.deliveryapp.manager.adapters.OrderDetailAdapter
import com.android.deliveryapp.util.Keys.Companion.clients
import com.android.deliveryapp.util.Keys.Companion.orders
import com.android.deliveryapp.util.ProductItem
import com.google.firebase.firestore.FirebaseFirestore

class ManagerOrderDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManagerOrderDetailBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var products: Array<ProductItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManagerOrderDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        products = emptyArray()

        val email = intent.getStringExtra("clientEmail")
        val date = intent.getStringExtra("orderDate")

        if (email != null && date != null) {

            binding.orderInfo.text = getString(R.string.order_info, email)

            getOrderDetails(firestore, email, date)
        }

        // show a back arrow button in actionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.selectRidersBtn.setOnClickListener {
            val intent = Intent(this@ManagerOrderDetailActivity,
                    RidersListActivity::class.java)
            intent.putExtra("clientEmail", email)
            intent.putExtra("orderDate", date)

            startActivity(intent)
        }
    }

    private fun getOrderDetails(firestore: FirebaseFirestore, email: String, date: String) {
        firestore.collection(clients).document(email)
            .collection(orders).document(date)
            .get()
            .addOnSuccessListener { result ->
                var price = 0.00
                var quantity: Long = 0
                var title = ""

                for (field in result.get("products") as ArrayList<Map<String, Any?>>) {
                    for (item in field) {
                        when(item.key) {
                            "price" -> price = item.value as Double
                            "quantity" -> quantity = item.value as Long
                            "title" -> title = item.value as String
                        }
                    }
                    products = products.plus(ProductItem("",
                            title,
                            "",
                            price,
                            quantity.toInt()))
                }

                updateView()
            }
            .addOnFailureListener { e ->
                Log.w("FIREBASE_FIRESTORE", "Error getting data", e)
                Toast.makeText(baseContext,
                    getString(R.string.failure_data),
                    Toast.LENGTH_LONG).show()
            }
    }

    private fun updateView() {
        if (products.isNotEmpty()) {
            binding.orderProductList.visibility = View.VISIBLE

            binding.orderProductList.adapter = OrderDetailAdapter(
                this,
                    R.layout.manager_order_detail_list_element,
                products
            )
        } else {
            binding.orderProductList.visibility = View.INVISIBLE

            Toast.makeText(baseContext,
                getString(R.string.failure_data),
                Toast.LENGTH_LONG).show()

            finish()
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