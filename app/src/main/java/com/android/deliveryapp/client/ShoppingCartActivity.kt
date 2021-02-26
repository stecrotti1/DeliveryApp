package com.android.deliveryapp.client

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.deliveryapp.R
import com.android.deliveryapp.client.adapters.ShoppingCartArrayAdapter
import com.android.deliveryapp.databinding.ActivityShoppingCartBinding
import com.android.deliveryapp.util.Keys.Companion.clients
import com.android.deliveryapp.util.Keys.Companion.hasLocation
import com.android.deliveryapp.util.Keys.Companion.orders
import com.android.deliveryapp.util.Keys.Companion.productListFirebase
import com.android.deliveryapp.util.Keys.Companion.productsDocument
import com.android.deliveryapp.util.Keys.Companion.shoppingCart
import com.android.deliveryapp.util.Keys.Companion.userInfo
import com.android.deliveryapp.util.ProductItem
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DateFormat
import java.util.*

class ShoppingCartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShoppingCartBinding
    private lateinit var firestore: FirebaseFirestore // shopping cart
    private lateinit var database: FirebaseDatabase // orders
    private lateinit var auth: FirebaseAuth
    private lateinit var products: Array<ProductItem>

    private var total: Double = 0.00

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShoppingCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        database = FirebaseDatabase.getInstance()

        auth = FirebaseAuth.getInstance()

        fetchItemsFromCloud()

        // show a back arrow button in actionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onStart() {
        super.onStart()

        /*************************+***** PLACE ORDER DIALOG ***************************************/

        binding.checkoutBtn.setOnClickListener {
            // check if user has set a location
            val user = auth.currentUser
            val sharedPreferences = getSharedPreferences(userInfo, Context.MODE_PRIVATE)

            // has location
            if (sharedPreferences.getBoolean(hasLocation, false) && user != null) {
                val dialogView = LayoutInflater.from(this).inflate(R.layout.checkout_dialog, null)

                // radio group has "credit cart" as set default checked
                var paymentType = ""

                val dialog: AlertDialog?

                val dialogBuilder = AlertDialog.Builder(this)
                        .setView(dialogView)
                        .setTitle(getString(R.string.place_order))

                val totalPrice: TextView = dialogView.findViewById(R.id.totalPriceDialog)
                totalPrice.text = binding.totalPriceLabel.text

                val paymentRadioGroup: RadioGroup = dialogView.findViewById(R.id.paymentOptions)

                val creditCardRadioButton: RadioButton = dialogView.findViewById(R.id.creditCard)
                val cashRadioButton: RadioButton = dialogView.findViewById(R.id.cash)

                val placeOrderBtn: ExtendedFloatingActionButton = dialogView.findViewById(R.id.placeOrderBtn)

                dialog = dialogBuilder.create()
                dialog.show()

                paymentRadioGroup.setOnCheckedChangeListener { _, i ->
                    if (creditCardRadioButton.isChecked && creditCardRadioButton.id == i) {
                        paymentType = getString(R.string.credit_card)
                    }
                    else if (cashRadioButton.isChecked && cashRadioButton.id == i) {
                        paymentType = getString(R.string.cash)
                    }
                }

                placeOrderBtn.setOnClickListener {
                    if (paymentType.isNotEmpty()) {
                        val reference = database.getReference(productListFirebase)

                        createOrder(firestore, reference, user, paymentType)
                        dialog.dismiss()
                    } else {
                        Toast.makeText(
                            baseContext,
                            getString(R.string.please_select_payment),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

            /*************************************************************************************/

            } else {

                /******************** NO LOCATION DIALOG *****************************************/

                val dialogView = LayoutInflater.from(this).inflate(R.layout.location_error_dialog, null)

                val dialog: AlertDialog?

                val errorButton: ExtendedFloatingActionButton = dialogView.findViewById(R.id.errorButton)

                val dialogBuilder = AlertDialog.Builder(this)
                        .setView(dialogView)
                        .setTitle(getString(R.string.error))

                dialog = dialogBuilder.create()
                dialog.show()

                // return to profile so client can set his location
                errorButton.setOnClickListener {
                    dialog.dismiss()
                    startActivity(Intent(this@ShoppingCartActivity, ClientProfileActivity::class.java))
                }
            }
        }
    }

    private fun getDate(): String {
        val today: Date = Calendar.getInstance().time

        return DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM).format(today)
    }

    private fun createOrder(firestore: FirebaseFirestore, reference: DatabaseReference, user: FirebaseUser, paymentType: String) {
        val today = getDate()

        val productMap = mapOf(
            "products" to products.toList()
        )

        // FIXME: 25/02/2021

        binding.emptyCartLabel.text = productMap.size.toString()

        val entry = mapOf(
                "total" to total,
                "payment" to paymentType,
                "date" to today
        )

        val orderEntry = mapOf(
            user.email!! to today
        )

        firestore.collection(clients).document(user.email!!)
                .collection(orders).document(today)
                .set(entry)
                .addOnSuccessListener {

                    firestore.collection(clients).document(user.email!!)
                        .collection(orders).document(today)
                        .collection(productsDocument).document()
                        .set(productMap)
                        .addOnSuccessListener {
                            // set user order in firestore so manager can see them
                            firestore.collection(orders).document()
                                .set(orderEntry)
                                .addOnSuccessListener {
                                    for (item in products) {
                                        updateProductQuantity(reference, item.title, item.quantity)
                                    }

                                    // empty the shopping cart
                                    emptyShoppingCart(firestore, user.email!!)
                                    products = emptyArray()

                                    // update view
                                    updateView(products)
                                }
                                .addOnFailureListener { e ->
                                    Log.w("FIREBASE_DATABASE", "Failed to upload data", e)
                                    Toast.makeText(baseContext,
                                        getString(R.string.order_failure),
                                        Toast.LENGTH_LONG).show()
                                }
                        }
                        .addOnFailureListener { e ->
                            Log.w("FIREBASE_FIRESTORE", "Failed to upload data", e)
                            Toast.makeText(baseContext,
                                getString(R.string.order_failure),
                                Toast.LENGTH_LONG).show()
                        }
                }
                .addOnFailureListener { e ->
                    Log.w("FIREBASE_FIRESTORE", "Failed to upload data", e)
                    Toast.makeText(baseContext,
                        getString(R.string.order_failure),
                        Toast.LENGTH_LONG).show()
                }
    }

    /**
     * Update the product quantity on firebase firestore
     */
    private fun updateProductQuantity(reference: DatabaseReference, productTitle: String, quantity: Int) {
        reference.child(productTitle).child("quantity")
            .get()
            .addOnSuccessListener { result ->
                val oldQuantity = result.value as Long

                reference.child(productTitle).child("quantity")
                    .setValue((oldQuantity - quantity))
                    .addOnSuccessListener {
                        Log.d("FIREBASE_DATABASE",
                            "Data uploaded with success")
                        Toast.makeText(baseContext,
                            getString(R.string.order_success),
                            Toast.LENGTH_LONG).show()

                    }
                    .addOnFailureListener { e ->
                        Log.w("FIREBASE_DATABASE",
                            "Failed to upload data",
                            e)
                        Toast.makeText(baseContext,
                            getString(R.string.order_failure),
                            Toast.LENGTH_LONG).show()
                    }
            }
    }

    private fun emptyShoppingCart(firestore: FirebaseFirestore, userEmail: String) {
        firestore.collection(clients).document(userEmail)
                .collection(shoppingCart)
                .get()
                .addOnSuccessListener { result ->
                    for (document in result.documents) {
                        document.reference.delete()
                    }
                    Log.d("FIREBASE_FIRESTORE", "Documents deleted")
                }
                .addOnFailureListener {
                    Log.w("FIREBASE_FIRESTORE", "Error deleting documents")
                }
    }

    /**
     * Get the total price
     */
    private fun getTotalPrice(): Double {
        for (item in products) {
            total += (item.price * item.quantity)

        }
        return total // 2 decimals
    }

    /**
     * Fetch the shopping cart items from cloud
     */
    private fun fetchItemsFromCloud() {
        total = 0.00
        products = emptyArray()

        val user = auth.currentUser

        if (user != null) {
            firestore.collection(clients).document(user.email!!)
                    .collection(shoppingCart)
                    .get()
                    .addOnSuccessListener { result ->
                        var title = ""
                        var price = 0.00
                        var qty: Long = 0

                        for (document in result.documents) {
                            for (item in document.data as Map<String, Any?>) {
                                when (item.key) {
                                    "title" -> title = item.value as String
                                    "price" -> price = item.value as Double
                                    "qty" -> qty = item.value as Long
                                }
                            }
                            products = products.plus(
                                    ProductItem("", title, "", price, qty.toInt())
                            )
                        }

                        // update view
                        updateView(products)
                    }
                    .addOnFailureListener { e ->
                        Log.w("FIREBASE_FIRESTORE", "Error fetching data", e)
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

    /**
     * Update the view: if there are products in the shopping cart, then show them, otherwise
     * show "empty cart"
     */
    private fun updateView(products: Array<ProductItem>) {
        // if client has put some items in shopping cart then he can place the order
        if (products.isNotEmpty()) {
            binding.emptyCartLabel.visibility = View.INVISIBLE
            binding.checkoutBtn.visibility = View.VISIBLE
            binding.shoppingListView.visibility = View.VISIBLE

            binding.shoppingListView.adapter = ShoppingCartArrayAdapter(
                    this,
                    R.layout.list_element_shopping_cart,
                    products
            )
            binding.totalPriceLabel.text = String.format("%s %.2f", getString(R.string.total_price), getTotalPrice())

        } else { // empty cart
            binding.emptyCartLabel.visibility = View.VISIBLE
            binding.checkoutBtn.visibility = View.INVISIBLE
            binding.shoppingListView.visibility = View.INVISIBLE

            binding.totalPriceLabel.text = ("${getString(R.string.total_price)} 0.00 €")
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

    override fun onDestroy() {
        super.onDestroy()
        products = emptyArray()
        total = 0.00
    }
}