package com.test.loginfirebase.data

data class AiMessage(
    var message: String,
    var sender: String,
    var timeStamp: Long? = System.currentTimeMillis(),
    var dateStamp: Long? = System.currentTimeMillis()
)
