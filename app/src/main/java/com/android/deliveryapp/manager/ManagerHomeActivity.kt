package com.android.deliveryapp.manager

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import com.android.deliveryapp.manager.adapters.ManagerArrayAdapter
import com.android.deliveryapp.util.Keys
import com.android.deliveryapp.util.Keys.Companion.productListFirebase
import com.android.deliveryapp.util.Keys.Companion.userInfo
import com.android.deliveryapp.util.Keys.Companion.userType
import com.android.deliveryapp.util.ProductItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore

class ManagerHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManagerHomeBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var productList: Array<ProductItem>
    private lateinit var firestore: FirebaseFirestore

    private val PERMISSION_CODE = 1000

    private val channelID = "1"
    private val notificationID = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManagerHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()
        val databaseRef = database.getReference(productListFirebase)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val user = auth.currentUser

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
            if (checkSelfPermission(Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_DENIED) {
                // permission not enabled
                val permission = arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                requestPermissions(permission, PERMISSION_CODE)
            } else {
                startActivity(Intent(this@ManagerHomeActivity, AddProductActivity::class.java))
            }
        }
    }

    override fun onStart() {
        super.onStart()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(this@ManagerHomeActivity, ManagerOrderActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(this@ManagerHomeActivity,
                0,
                intent,
                0)

        listenForNewOrders(firestore, pendingIntent, notificationManager)
    }

    private fun processItems(snapshot: DataSnapshot): Array<ProductItem> {
        var imageUrl = ""
        var title = ""
        var desc = ""
        var price = 0.00
        var qty: Long = 0

        var array = emptyArray<ProductItem>()

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
            array = array.plus(ProductItem(imageUrl, title, desc, price, qty.toInt()))
        }
        return array
    }

    /**
     * Listen for new orders and sends a notification
     * @param firestore firestore instance
     */
    private fun listenForNewOrders(firestore: FirebaseFirestore,
                                   pendingIntent: PendingIntent,
                                   notificationManager: NotificationManager) {
        firestore.collection(Keys.orders).addSnapshotListener { value, error ->
            if (error != null) {
                Log.w("FIREBASE_FIRESTORE", "Listen failed", error)
                return@addSnapshotListener
            } else {
                if (value!!.documents.size > 0) {
                    createNotification(pendingIntent, notificationManager)
                    createNotificationChannel(channelID,
                            getString(R.string.app_name),
                            getString(R.string.new_order_notification_msg),
                            notificationManager)
                }
            }

        }
    }

    private fun createNotification(pendingIntent: PendingIntent, notificationManager: NotificationManager) {
        val notification = Notification.Builder(this@ManagerHomeActivity, channelID)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(getString(R.string.new_order_notification_msg))
                .setContentText(getString(R.string.new_order_description))
                .setChannelId(channelID)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

        notificationManager.notify(notificationID, notification)
    }

    private fun createNotificationChannel(id : String, name: String, description: String, notificationManager: NotificationManager) {
        val priority = NotificationManager.IMPORTANCE_HIGH

        val channel = NotificationChannel(id, name, priority)

        channel.description = description

        notificationManager.createNotificationChannel(channel)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.manager_home_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        auth = FirebaseAuth.getInstance()
        deleteSharedPreferences(userType)
        return when (item.itemId) {
            R.id.managerProfile -> {
                startActivity(Intent(this@ManagerHomeActivity, ManagerProfileActivity::class.java))
                true
            }
            R.id.ridersList -> {
                // TODO: 19/02/2021 activity list riders
                true
            }
            R.id.orders -> {
                startActivity(Intent(this@ManagerHomeActivity, ManagerOrderActivity::class.java))
                true
            }
            R.id.logout -> {
                auth.signOut()
                val sharedPreferences = getSharedPreferences(userInfo, Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.clear() // delete all shared preferences
                editor.apply()

                startActivity(Intent(this@ManagerHomeActivity, LoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}