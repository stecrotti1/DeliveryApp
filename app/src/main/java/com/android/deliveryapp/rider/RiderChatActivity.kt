package com.android.deliveryapp.rider

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.android.deliveryapp.databinding.ActivityRiderChatBinding
import com.android.deliveryapp.util.Keys.Companion.chatCollection
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class RiderChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRiderChatBinding

    companion object {
        const val NAME = "NAME"
        const val TEXT = "TEXT"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRiderChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // show a back arrow button in actionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val riderEmail = intent.getStringExtra("riderEmail")
        val recipientEmail = intent.getStringExtra("recipientEmail")

        val firestoreChat by lazy {
            FirebaseFirestore.getInstance().collection(chatCollection)
                .document("$riderEmail|$recipientEmail")
        }

        updateChat(firestoreChat)

        binding.sendMsgBtn.setOnClickListener {
            sendMessage(firestoreChat)
            binding.message.text?.clear()
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

    private fun sendMessage(reference: DocumentReference) {
        val newMessage = mapOf(
            NAME to "Rider",
            TEXT to binding.message.text.toString()
        )

        reference.set(newMessage)
            .addOnSuccessListener {
                Log.d("FIRESTORE_CHAT", "Message sent")
            }
            .addOnFailureListener { e ->
                Log.e("ERROR", e.message.toString())
            }
    }

    private fun updateChat(reference: DocumentReference) {
        reference.addSnapshotListener { value, error ->
            when {
            error != null -> Log.e("ERROR", error.message.toString())
                value != null && value.exists() -> {
                    with(value) {
                        binding.messageTextView.append("${data?.get(NAME)}:${data?.get(TEXT)}\n")
                    }
                }
            }
        }
    }
}