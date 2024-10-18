package com.test.loginfirebase.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference


class FirebaseUtil {

    fun currentUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    fun isLoggedIn(): Boolean {
        if (currentUserId() != null) {
            return true
        }
        return false
    }


}