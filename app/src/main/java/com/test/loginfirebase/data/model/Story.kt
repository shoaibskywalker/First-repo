package com.test.loginfirebase.data.model


data class Story(
    val imageUrl: String = "",
    val name: String = "",
    val timestamp: Long = System.currentTimeMillis(), // Optional timestamp for ordering
    val userId: String = "",
)
