package com.test.loginfirebase.data


data class Message(
    var message: String? = null,
    var senderId: String? = null,
    var timeStamp: Long? = System.currentTimeMillis(),
    var dateStamp: Long? = System.currentTimeMillis(),
    var messageId: String = ""
)
