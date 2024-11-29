package com.test.loginfirebase.utils

import com.google.firebase.auth.FirebaseAuth


class FirebaseUtil {

    fun currentUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

}