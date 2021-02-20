package com.android.deliveryapp.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.deliveryapp.R
import com.android.deliveryapp.databinding.ActivityClientHomeBinding
import com.android.deliveryapp.profile.ClientProfileActivity
import com.android.deliveryapp.util.CustomArrayAdapter
import com.android.deliveryapp.util.Keys.Companion.productListFirebase
import com.android.deliveryapp.util.ProductItem
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ClientHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClientHomeBinding
    //private lateinit var storage: StorageReference // where the images are stored
    private lateinit var database: FirebaseDatabase // product names and prices

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // FIXME: 20/02/2021 database images won't show 
        //storage = FirebaseStorage.getInstance().reference
        database = FirebaseDatabase.getInstance()

        val databaseRef = database.getReference(productListFirebase)

        var productList: Array<ProductItem>
        Thread {
            databaseRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    productList = processItems(snapshot) // create the product list

                    val adapter = CustomArrayAdapter(
                        this@ClientHomeActivity,
                        R.layout.list_element,
                        productList
                    )

                    binding.productListView.adapter = adapter

                    runOnUiThread {
                        adapter.notifyDataSetChanged()
                    }
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
        }.start()


        /*
        storage.child("productImages/").listAll()
                .addOnSuccessListener { list ->
                    for (image in list.items) {
                        image.downloadUrl.addOnSuccessListener { url ->
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                            baseContext,
                            getString(R.string.image_loading_error),
                            Toast.LENGTH_LONG
                    ).show()
                    Log.w("FIREBASE_STORAGE", "Failed to retrieve items", e)
                }
         */
    }

    /**
     * @param snapshot the firebase realtime database snapshot
     * @return an array containing data related to the market products
     */
    private fun processItems(snapshot: DataSnapshot): Array<ProductItem> {
        var imageUrl = ""
        var title = ""
        var price = ""
        val qty = ""

        val array = emptyArray<ProductItem>()

        for (child in snapshot.children) {
            for (item in child.children) {
                when (item.key) {
                    "image" -> imageUrl = item.value as String
                    "title" -> title = item.value as String
                    "price" -> price = item.value as String
                    // quantity isn't needed by the user so it remains always ""
                }
            }
            array.plus(ProductItem(imageUrl, title, price, qty))
        }
        return array
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.client_home_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
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
            else -> super.onOptionsItemSelected(item)
        }
    }
}