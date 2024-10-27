package com.test.loginfirebase.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.test.loginfirebase.R
import com.test.loginfirebase.VoiceCall

class IncomingCallService : FirebaseMessagingService() {

   /* private lateinit var pendingIntent: PendingIntent

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d("FCM", "From: ${remoteMessage.from}")
        Log.d("FCM", "Notification Title: ${remoteMessage.notification?.title}")
        Log.d("FCM", "Notification Body: ${remoteMessage.notification?.body}")

        // Check for data payload
        if (remoteMessage.data.isNotEmpty()) {
            val receiverUid = remoteMessage.data["receiverUid"]
            val receiverName = remoteMessage.data["receiverName"]
            val senderName = remoteMessage.data["senderName"]
            val senderUid = remoteMessage.data["senderUid"]
            val isVoiceCall = remoteMessage.data["isVoiceCall"] == "true"

            // Log data payload
            Log.d("FCM", "Sender Name: $senderName")
            Log.d("FCM", "Sender UID: $senderUid")

            // Show the notification for an incoming call
            if (isVoiceCall) {
                showIncomingCallNotification(senderName, senderUid,receiverUid,receiverName)
            }
        }
    }

    private fun showIncomingCallNotification(senderName: String?, senderUid: String?,receiverUid: String?, receiverName: String?) {
        // Create an intent for opening CallActivity when notification is clicked
        val intent = Intent(this, VoiceCall::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("senderUid", senderUid)
            putExtra("senderName", senderName)
            putExtra("receiverUid", receiverUid)
            putExtra("receiverName", receiverName)
        }
        pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val channelId = "incoming_call_channel"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.shoaib) // Use a suitable call icon
            .setContentTitle("Incoming Call from $senderName")
            .setContentText("Tap to answer the call")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // For Android Oreo and above, create a notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Incoming Call Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(1, notificationBuilder.build()) // Use a unique ID for incoming call notifications
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Update the FCM token to your backend if needed
    }*/
}
