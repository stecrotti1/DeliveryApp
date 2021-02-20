package com.android.deliveryapp.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

class ClientHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClientHomeBinding
    private lateinit var database: FirebaseDatabase // product names and prices
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // FIXME: 20/02/2021 ListView is blank
        database = FirebaseDatabase.getInstance()

        val databaseRef = database.getReference(productListFirebase)
        val auth = FirebaseAuth.getInstance()

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productList = processItems(snapshot) // create the product list



                val adapter = ClientArrayAdapter(this@ClientHomeActivity, R.layout.list_element, productList)
                binding.productListView.adapter = adapter
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


    /**
     * @param snapshot the firebase realtime database snapshot
     * @return an array containing data related to the market products
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
            if (qty != "0") {
                array = array.plus(ProductItem(imageUrl, title, price, qty))
            }
        }
         if (array.isEmpty()) {
             Toast.makeText(baseContext, "EMPTY", Toast.LENGTH_SHORT).show()
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
        when (item.itemId) {
            R.id.clientProfile -> {
                startActivity(Intent(this@ClientHomeActivity, ClientProfileActivity::class.java))

            }
            R.id.orders -> {
                // TODO: 07/02/2021 start activity orders or fragment??

            }
            R.id.shoppingCart -> {
                // TODO: 07/02/2021 start activity shopping cart or fragment??

            }
            R.id.logout -> {
                auth.signOut()
                startActivity(Intent(this@ClientHomeActivity, LoginActivity::class.java))
                finish()
            }

        }
        return super.onOptionsItemSelected(item)
    }
}