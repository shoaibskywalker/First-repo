package com.test.loginfirebase.data.model

data class NotificationRequest(
    val message: Messagee
)

data class Messagee(
    val token: String,
    val notification: Notification,
    val data: Map<String, String> // Include data map for additional info

)

data class Notification(
    val title: String,
    val body: String
)