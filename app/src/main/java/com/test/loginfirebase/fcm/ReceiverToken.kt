package com.test.loginfirebase.fcm

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


     fun getReceiverFcmToken(receiverUid: String, onTokenReceived: (String) -> Unit) {
        val userRef = FirebaseDatabase.getInstance().getReference("Users").child(receiverUid)
        userRef.child("fcmToken").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val token = snapshot.getValue(String::class.java)
                if (token != null) {
                    onTokenReceived(token)
                    Log.d("Is Receiver token available", "Receiver token is available")

                } else {
                    Log.d("Is Receiver token available", "Receiver token is not available")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Is Receiver token available", "Failed to fetch Receiver token")
            }
        })
    }

