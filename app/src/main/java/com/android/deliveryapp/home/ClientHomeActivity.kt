package com.android.deliveryapp.home

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
import com.android.deliveryapp.databinding.ActivityClientHomeBinding
import com.android.deliveryapp.profile.ClientProfileActivity
import com.android.deliveryapp.util.ClientArrayAdapter
import com.android.deliveryapp.util.Keys.Companion.productListFirebase
import com.android.deliveryapp.util.ProductItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class ClientHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClientHomeBinding
    private lateinit var database: FirebaseDatabase // product names and prices
    private lateinit var auth: FirebaseAuth
    private lateinit var productList: Array<ProductItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()

        val databaseRef = database.getReference(productListFirebase)
        auth = FirebaseAuth.getInstance()

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productList = processItems(snapshot) // create the product list

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
    }

    override fun onStart() {
        super.onStart()

        binding.productListView.setOnItemClickListener { _, _, i, _ ->
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
            dialogProductPrice?.text = productList[i].price

            val dialogBuilder = AlertDialog.Builder(this)
                    .setView(dialogView)
                    .setTitle(productTitle)

            dialog = dialogBuilder.create()
            dialog.show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        productList = emptyArray()
    }


    /**
     * @param snapshot the firebase realtime database snapshot
     * @return an array containing data related to the market products
     * if an item has quantity 0 it will not be shown at the user (CLIENT)
     */
    private fun processItems(snapshot: DataSnapshot): Array<ProductItem> {
        var imageUrl = ""
        var title = ""
        var price = ""
        var qty = ""

        var array = emptyArray<ProductItem>()

        for (child in snapshot.children) {
            for (item in child.children) {
                when (item.key) {
                    "image" -> imageUrl = item.value.toString()
                    "title" -> title = item.value.toString()
                    "price" -> price = item.value.toString()
                    "quantity" -> qty = item.value.toString()
                }
            }
            if (qty != "0") { // don't add items with qty 0
                array = array.plus(ProductItem(imageUrl, title, price, qty))
            }
        }
        return array
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
                // TODO: 07/02/2021 start activity orders or fragment??
                true
            }
            R.id.shoppingCart -> {
                // TODO: 07/02/2021 start activity shopping cart or fragment??
                true
            }
            R.id.logout -> {
                auth.signOut()
                startActivity(Intent(this@ClientHomeActivity, LoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}