package com.test.loginfirebase.fcm

import android.content.Context
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ServerToken(private val context: Context) {
    private var cachedToken: String? = null

    suspend fun getServerToken(): String = withContext(Dispatchers.IO) {

        // Return the cached token if it exists
        if (cachedToken != null) {
            return@withContext cachedToken!!
        }

        val assetManager = context.assets

        val inputStream = assetManager.open("newService.json")

// Load credentials and generate the access token
        val credentials: GoogleCredentials = com.google.auth.oauth2.ServiceAccountCredentials
            .fromStream(inputStream)
            .createScoped(
                listOf(
                    "https://www.googleapis.com/auth/userinfo.email",
                    "https://www.googleapis.com/auth/firebase.database",
                    "https://www.googleapis.com/auth/firebase.messaging"
                )
            )

        credentials.refreshIfExpired()

        cachedToken = credentials.accessToken.tokenValue

        return@withContext cachedToken!!
    }

}
