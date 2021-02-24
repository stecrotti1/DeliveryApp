package com.android.deliveryapp.client

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import coil.load
import coil.transform.CircleCropTransformation
import com.android.deliveryapp.LoginActivity
import com.android.deliveryapp.R
import com.android.deliveryapp.client.adapters.ClientArrayAdapter
import com.android.deliveryapp.databinding.ActivityClientHomeBinding
import com.android.deliveryapp.util.Keys
import com.android.deliveryapp.util.Keys.Companion.clients
import com.android.deliveryapp.util.Keys.Companion.productListFirebase
import com.android.deliveryapp.util.Keys.Companion.shoppingCart
import com.android.deliveryapp.util.ProductItem
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class ClientHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClientHomeBinding
    private lateinit var database: FirebaseDatabase // product names and prices
    private lateinit var firestore: FirebaseFirestore // shopping cart
    private lateinit var auth: FirebaseAuth
    private lateinit var productList: Array<ProductItem>

    private var singleProductCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()

        firestore = FirebaseFirestore.getInstance()

        val databaseRef = database.getReference(productListFirebase)
        auth = FirebaseAuth.getInstance()

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                processItems(snapshot) // create the product list

                binding.productListView.adapter = ClientArrayAdapter(
                        this@ClientHomeActivity, R.layout.list_element, productList
                )
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                        baseContext,
                        getString(R.string.image_loading_error),
                        Toast.LENGTH_LONG
                ).show()
                Log.w("FIREBASE_DATABASE", "Failed to retrieve items", error.toException())
            }
        })

        binding.shoppingCartButton.setOnClickListener {
            startActivity(Intent(this@ClientHomeActivity, ShoppingCartActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()

        /***************************** ADD TO CART DIALOG ****************************************/

        binding.productListView.setOnItemClickListener { _, _, i, _ ->
            singleProductCount = 0
            val productTitle: String = productList[i].title.capitalize(Locale.ROOT) // capitalize first letter

            val dialogView = LayoutInflater.from(this).inflate(R.layout.product_dialog, null)

            val dialog: AlertDialog?

            val dialogImage: ImageView? = dialogView.findViewById(R.id.productImageDialog)
            dialogImage?.load(productList[i].imgUrl) {
                transformations(CircleCropTransformation())
                error(R.mipmap.ic_launcher_round)
                crossfade(true)
                build()
            }

            val dialogProductPrice: TextView? = dialogView.findViewById(R.id.productPriceDialog)
            dialogProductPrice?.text = String.format( "%.2f€", productList[i].price)

            val productDesc: TextView? = dialogView.findViewById(R.id.descriptionDialog)
            productDesc?.text = productList[i].description

            val productQty: TextInputEditText = dialogView.findViewById(R.id.productQtyCounter)
            productQty.setText("$singleProductCount")
            productQty.keyListener = null // not editable with keyboard but visible

            val removeQty: FloatingActionButton = dialogView.findViewById(R.id.minusButton)

            val addQty: FloatingActionButton = dialogView.findViewById(R.id.plusButton)

            val addToCart: FloatingActionButton = dialogView.findViewById(R.id.addProductButton)

            val dialogBuilder = AlertDialog.Builder(this)
                    .setView(dialogView)
                    .setTitle(productTitle)

            dialog = dialogBuilder.create()
            dialog.show()

            /************************ ALERT DIALOG BUTTONS ****************************************/

            removeQty.setOnClickListener {
                if (singleProductCount == 0) { // remove product from cart
                    removeFromShoppintCart(auth, firestore, productList[i].title)

                    dialog.dismiss()
                }
                else {
                    productQty.setText((--singleProductCount).toString())
                }
            }

            addQty.setOnClickListener {
                // product desired by the user == quantity available
                if (singleProductCount == productList[i].quantity) {
                    Toast.makeText(
                            baseContext,
                            getString(R.string.error_product_quantity),
                            Toast.LENGTH_SHORT
                    ).show()
                }
                else {
                    productQty.setText((++singleProductCount).toString())
                }
            }

            addToCart.setOnClickListener {
                if (singleProductCount == 0) {
                    Toast.makeText(
                            baseContext,
                            getString(R.string.please_add_quantity),
                            Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // add the new entry
                    addToShoppingCart(
                            auth,
                            firestore,
                            productList[i].title,
                            productList[i].price,
                            singleProductCount
                    )
                    dialog.dismiss()
                }
            }

            /*************************************************************************************/
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        productList = emptyArray()
        singleProductCount = 0
    }

    /**
     * @param snapshot the firebase realtime database snapshot
     * @return an array containing data related to the market products
     * if an item has quantity 0 it will not be shown at the user (CLIENT)
     */
    private fun processItems(snapshot: DataSnapshot) {
        var imageUrl = ""
        var title = ""
        var desc = ""
        var price = 0.00
        var qty: Long = 0

        productList = emptyArray()

        for (child in snapshot.children) {
            for (item in child.children) {
                when (item.key) {
                    "image" -> imageUrl = item.value as String
                    "title" -> title = item.value as String
                    "description" -> desc = item.value as String
                    "price" -> price = item.value as Double
                    "quantity" -> qty = item.value as Long
                }
            }
            if (qty.toInt() != 0) { // don't add items with qty 0
                productList = productList.plus(ProductItem(imageUrl, title, desc, price, qty.toInt()))
            }
        }
    }

    /**
     * add a new entry to the database with a subcollection
     * "user.id/shoppingCart/product.title/"
     * @param auth firebase auth instance
     * @param firestore firestore instance
     * @param title product title
     * @param price product price
     * @param quantity product quantity
     */
    private fun addToShoppingCart(
            auth: FirebaseAuth,
            firestore: FirebaseFirestore,
            title: String,
            price: Double,
            quantity: Int
    ) {
        val user = auth.currentUser

        if (user != null) {
            val entry = mapOf(
                    "title" to title,
                    "price" to price,
                    "qty" to quantity
            )

            // set entry under "user.orderEmail/shoppingCart/product.title"
            firestore.collection(clients).document(user.email!!)
                    .collection(shoppingCart).document(title)
                    .set(entry)
                    .addOnSuccessListener { documentRef ->
                        Log.d("FIREBASEFIRESTORE", "Document added with id: $documentRef")
                        Toast.makeText(
                                baseContext,
                                getString(R.string.add_cart_success),
                                Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener { e ->
                        Log.w("FIREBASEFIRESTORE", "Error adding document", e)
                        Toast.makeText(
                                baseContext,
                                getString(R.string.error_shopping_cart),
                                Toast.LENGTH_LONG
                        ).show()
                    }
        } else {
            auth.currentUser?.reload()
            Toast.makeText(
                    baseContext,
                    getString(R.string.error_shopping_cart),
                    Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Remove the document from the sub-collection "shoppingCart"
     * @param auth firebase auth instance
     * @param firestore firestore instance
     * @param title product title to be removed
     */
    private fun removeFromShoppintCart(
            auth: FirebaseAuth,
            firestore: FirebaseFirestore,
            title: String
    ) {
        val user = auth.currentUser

        if (user != null) {
            firestore.collection(clients).document(user.email!!)
                    .collection(shoppingCart).document(title)
                    .get()
                    .addOnSuccessListener { result ->
                        if (result.exists()) { // if it exists then remove it, otherwise, do nothing
                            result.reference.delete()
                            Log.d("FIREBASE_FIRESTORE", "Product removed with success")
                            Toast.makeText(baseContext,
                                    getString(R.string.product_removed_from_cart_success),
                                    Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener {
                        Log.w("FIREBASE_FIRESTORE", "Error removing product from cart")
                        Toast.makeText(baseContext,
                                getString(R.string.error_removing_from_cart),
                                Toast.LENGTH_LONG).show()
                    }
        } else {
            auth.currentUser?.reload()
            Toast.makeText(
                    baseContext,
                    getString(R.string.error_user_data),
                    Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.client_home_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        auth = FirebaseAuth.getInstance()
        return when (item.itemId) {
            R.id.clientProfile -> {
                startActivity(Intent(this@ClientHomeActivity, ClientProfileActivity::class.java))
                true
            }
            R.id.orders -> {
                startActivity(Intent(this@ClientHomeActivity, ClientOrdersActivity::class.java))
                true
            }
            R.id.shoppingCart -> {
                startActivity(Intent(this@ClientHomeActivity, ShoppingCartActivity::class.java))
                true
            }
            R.id.logout -> {
                auth.signOut()

                val sharedPreferences = getSharedPreferences(Keys.userInfo, Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.clear() // delete all shared preferences
                editor.apply()

                startActivity(Intent(this@ClientHomeActivity, LoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}