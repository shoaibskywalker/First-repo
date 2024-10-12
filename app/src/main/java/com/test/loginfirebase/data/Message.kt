package com.test.loginfirebase.data

import java.util.Date

data class Message(
    var message: String? = null,
    var senderId: String? = null,
    var timeStamp: Long? = System.currentTimeMillis(),
    var messageId: String = ""
)
