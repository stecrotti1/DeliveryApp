package com.android.deliveryapp.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.deliveryapp.R
import com.android.deliveryapp.databinding.ActivityRiderProfileBinding
import com.android.deliveryapp.home.RiderHomeActivity
import com.android.deliveryapp.util.Keys.Companion.riderStatus
import com.android.deliveryapp.util.Keys.Companion.riders
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RiderProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRiderProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRiderProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        database = FirebaseFirestore.getInstance()

        supportActionBar?.title = getString(R.string.rider)

        val user = auth.currentUser

        if (user != null) {
            binding.email.setText(user.email) // show email at the user
            binding.email.keyListener = null // not editable by user, but still visible

            binding.riderStatus.setOnCheckedChangeListener { _, isChecked ->
                GlobalScope.launch { // coroutine
                    uploadToCloud(database, user, isChecked)
                }
            }
        }
    }

    /**
     * Upload data to cloud
     */
    private fun uploadToCloud(database: FirebaseFirestore, user: FirebaseUser, isChecked: Boolean) {
        val entry = hashMapOf(riderStatus to isChecked)

        database.collection(riders)
            .document(user.email!!)
            .set(entry)
            .addOnSuccessListener { documentRef ->
                Log.d("FIREBASEFIRESTORE", "DocumentSnapshot added with $documentRef")
                Toast.makeText(baseContext, getString(R.string.rider_status_save_success), Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.w("FIREBASEFIRESTORE", "Error adding document", e)
                Toast.makeText(baseContext, getString(R.string.rider_status_save_failure), Toast.LENGTH_SHORT).show()
            }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.profile_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.homePage -> {
                startActivity(Intent(this@RiderProfileActivity, RiderHomeActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}