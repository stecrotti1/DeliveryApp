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
import com.android.deliveryapp.databinding.ActivityManagerHomeBinding
import com.android.deliveryapp.profile.ManagerProfileActivity
import com.android.deliveryapp.util.Keys.Companion.productListFirebase
import com.android.deliveryapp.util.ManagerArrayAdapter
import com.android.deliveryapp.util.ProductItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ManagerHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManagerHomeBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var productList: Array<ProductItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManagerHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()
        val databaseRef = database.getReference(productListFirebase)

        auth = FirebaseAuth.getInstance()

        databaseRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productList = processItems(snapshot)

                binding.productListView.adapter = ManagerArrayAdapter(
                        this@ManagerHomeActivity, R.layout.list_element, productList
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

        binding.addProductButton.setOnClickListener {
            // TODO: 19/02/2021 add product activity
        }
    }

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
            array = array.plus(ProductItem(imageUrl, title, price, qty))
        }
        return array
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.manager_home_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        auth = FirebaseAuth.getInstance()
        return when (item.itemId) {
            R.id.managerProfile -> {
                startActivity(Intent(this@ManagerHomeActivity, ManagerProfileActivity::class.java))
                true
            }
            R.id.ridersList -> {
                // TODO: 19/02/2021 activity list riders
                true
            }
            R.id.logout -> {
                auth.signOut()
                startActivity(Intent(this@ManagerHomeActivity, LoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}