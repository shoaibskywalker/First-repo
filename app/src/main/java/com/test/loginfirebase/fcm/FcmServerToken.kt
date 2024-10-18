package com.test.loginfirebase.fcm

import android.content.Context
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ServerToken(private val context: Context) {
    private var cachedToken: String? = null

    suspend fun getServerToken(): String = withContext(Dispatchers.IO) {

        // Return the cached token if it exists
        if (cachedToken != null) {
            return@withContext cachedToken!!
        }
        // Fetch the service account JSON from Firebase Remote Config
        val remoteConfig = Firebase.remoteConfig

        // Set default values if needed (can be skipped if using Firebase console defaults)
        remoteConfig.setDefaultsAsync(mapOf("service_account_json" to ""))

        // Fetch the remote config (you can set a fetch interval for caching, like 3600 seconds)
        remoteConfig.fetchAndActivate().await()

        // Get the service account JSON from Remote Config
        val serviceAccountJson = remoteConfig.getString("service_credential")

        if (serviceAccountJson.isEmpty()) {
            throw IllegalArgumentException("Service account JSON is missing in Remote Config")
        }

        val assetManager = context.assets

        val inputStream = serviceAccountJson.byteInputStream()


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
