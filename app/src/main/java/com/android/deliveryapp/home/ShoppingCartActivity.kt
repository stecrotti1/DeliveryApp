package com.android.deliveryapp.home

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.deliveryapp.R
import com.android.deliveryapp.databinding.ActivityShoppingCartBinding
import com.android.deliveryapp.util.Keys
import com.android.deliveryapp.util.Keys.Companion.clients
import com.android.deliveryapp.util.Keys.Companion.hasLocation
import com.android.deliveryapp.util.Keys.Companion.orders
import com.android.deliveryapp.util.Keys.Companion.shoppingCart
import com.android.deliveryapp.util.Keys.Companion.userInfo
import com.android.deliveryapp.util.PaymentType
import com.android.deliveryapp.util.ProductItem
import com.android.deliveryapp.util.ShoppingCartArrayAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ShoppingCartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShoppingCartBinding
    private lateinit var firestore: FirebaseFirestore // shopping cart
    private lateinit var database: FirebaseDatabase // orders
    private lateinit var auth: FirebaseAuth
    private lateinit var products: Array<ProductItem>

    private var total: Double = 0.00
    private var formatter = DateTimeFormatter.RFC_1123_DATE_TIME

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShoppingCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        database = FirebaseDatabase.getInstance()

        auth = FirebaseAuth.getInstance()

        fetchItemsFromCloud(firestore)

        // if client has put some items in shopping cart then he can place the order
        if (products.isNotEmpty()) {
            binding.emptyCartLabel.visibility = View.INVISIBLE
            binding.checkoutBtn.visibility = View.VISIBLE
            binding.shoppingListView.visibility = View.VISIBLE

            binding.totalPriceLabel.text = getTotalPrice()

        } else { // empty cart
            binding.emptyCartLabel.visibility = View.VISIBLE
            binding.checkoutBtn.visibility = View.INVISIBLE
            binding.shoppingListView.visibility = View.INVISIBLE

            binding.totalPriceLabel.text = ("${getString(R.string.total_price)} 0.00 €")
        }
    }

    override fun onStart() {
        super.onStart()

        /************************************ LISTVIEW ************************************/

        binding.shoppingListView.setOnItemClickListener { _, view, i, _ ->
            val removeItemButton = view.findViewById<FloatingActionButton>(R.id.removeItemButton)

            var quantity = products[i].quantity.toInt()

            removeItemButton.setOnClickListener {
                if (quantity == 0) {
                    removeItem(products[i])
                } else {
                    products[i].quantity = (--quantity).toString()
                }
                // update the view
                binding.shoppingListView.adapter = ShoppingCartArrayAdapter(
                        this, R.layout.list_element_shopping_cart, products
                )

                binding.totalPriceLabel.text = getTotalPrice()
            }
        }

        /****************************************************************************************/

        /******************************** PLACE ORDER ******************************************/

        binding.checkoutBtn.setOnClickListener {
            // check if user has set a location
            val user = auth.currentUser
            val sharedPreferences = getSharedPreferences(userInfo, Context.MODE_PRIVATE)

            // has location
            if (sharedPreferences.getBoolean(hasLocation, false) || user != null) {
                val dialogView = LayoutInflater.from(this).inflate(R.layout.checkout_dialog, null)

                var paymentType: PaymentType = PaymentType.CASH

                val dialog: AlertDialog?

                val totalPrice: TextView? = dialogView.findViewById(R.id.totalPriceDialog)
                totalPrice?.text = getTotalPrice()

                val payment: RadioGroup? = dialogView.findViewById(R.id.paymentOptions)

                val placeOrderBtn: FloatingActionButton = dialogView.findViewById(R.id.placeOrderBtn)

                val dialogBuilder = AlertDialog.Builder(this)
                        .setView(dialogView)
                        .setTitle(getString(R.string.place_order))

                dialog = dialogBuilder.create()
                dialog.show()

                payment?.setOnClickListener {
                    when(payment.checkedRadioButtonId) {
                        R.id.cash -> paymentType = PaymentType.CASH
                        R.id.creditCard -> paymentType = PaymentType.CREDIT_CARD
                    }
                }

                /************************ ALERT DIALOG BUTTONS ****************************************/

                placeOrderBtn.setOnClickListener {
                    var position: GeoPoint?
                    firestore.collection(clients).document(user?.email!!) // fetch user address from cloud
                            .get()
                            .addOnSuccessListener { result ->
                                position = result.getGeoPoint(Keys.clientAddress)
                                createOrder(database.reference, user, position, paymentType)
                            }
                            .addOnFailureListener { e ->
                                Log.w("FIREBASEFIRESTORE", "Failed to get client position", e)
                            }
                }

                /**********************************************************************************/

            } else {
                Toast.makeText(
                        baseContext,
                        getString(R.string.error_user_data),
                        Toast.LENGTH_LONG
                ).show()
                Log.w("FIREBASEAUTH", "Error fetching user")
            }

        }
    }

    private fun createOrder(reference: DatabaseReference, user: FirebaseUser, position: GeoPoint?, paymentType: PaymentType) {
        val today = LocalDate.now().format(formatter)

        var productMap: Map<String, String> = emptyMap()

        for (item in products) {
            productMap = productMap.plus("title" to item.title)
            productMap = productMap.plus("quantity" to item.quantity)
        }

        val entry = mapOf(
                "products" to productMap,
                "total" to total,
                "position" to position,
                "payment" to paymentType.toString(),
                "date" to today
        )

        reference.child(orders).child(user.email!!).setValue(entry)
                .addOnSuccessListener {
                    Log.d("FIREBASE_DATABASE", "Data uploaded with success")
                    Toast.makeText(baseContext,
                            getString(R.string.order_success),
                            Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener { e ->
                    Log.w("FIREBASE_DATABASE", "Failed to upload data", e)
                    Toast.makeText(baseContext,
                            getString(R.string.order_failure),
                            Toast.LENGTH_LONG).show()
                }
    }

    /**
     * Get the total price
     */
    private fun getTotalPrice(): String {
        for (item in products) {
            for (price in item.price) {
                total += price.toDouble()
            }
        }
        return ("${getString(R.string.total_price)} $total €")
    }

    /**
     * Remove an item from the product list
     * @param item the item to be removed
     */
    private fun removeItem(item: ProductItem) {
        if (products.isNotEmpty()) {
            val temp = products.toMutableList()

            temp.remove(item)
            products = emptyArray()
            products = temp.toTypedArray()
        }
    }

    /**
     * Fetch the shopping cart items from cloud
     * @param firestore instance
     */
    private fun fetchItemsFromCloud(firestore: FirebaseFirestore) {
        total = 0.00
        products = emptyArray()

        val user = auth.currentUser

        if (user != null) {
            firestore.collection(clients).document(user.email!!)
                    .get()
                    .addOnSuccessListener { result ->
                        var title = ""
                        var price = ""
                        var qty = "" // quantity chosen by the client

                        // get Map<> shoppingCart in Cloud
                        for (item in result.get(shoppingCart) as Map<*, *>) {
                            when (item.key) {
                                "title" -> {
                                    title = item.value as String
                                    price = item.value as String
                                    qty = item.value as String
                                }
                            }
                            products = products.plus(ProductItem(
                                    "",
                                    title,
                                    "",
                                    price,
                                    qty))
                        }

                        binding.shoppingListView.adapter = ShoppingCartArrayAdapter(this,
                                R.layout.list_element_shopping_cart,
                                products)
                    }
                    .addOnFailureListener { e ->
                        Log.w("FIREBASEFIRESTORE", "Error fetching data", e)
                        Toast.makeText(
                                baseContext,
                                getString(R.string.error_user_data),
                                Toast.LENGTH_LONG
                        ).show()
                    }

        } else {
            Toast.makeText(
                    baseContext,
                    getString(R.string.error_user_data),
                    Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        products = emptyArray()
        total = 0.00
    }
}