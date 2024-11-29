package com.test.loginfirebase.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.test.loginfirebase.ChatActivity
import com.test.loginfirebase.R
import com.test.loginfirebase.utils.sessionManager.UserSessionManager

class MyFirebaseMessagingService: FirebaseMessagingService() {

    private lateinit var pendingIntent: PendingIntent
    private lateinit var prefs: UserSessionManager


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)


        // Check for data payload
        if (remoteMessage.data.isNotEmpty()) {
            val receiverUid = remoteMessage.data["receiverUid"]
            val receiverName = remoteMessage.data["receiverName"]
            val message = remoteMessage.data["message"]
            val senderName = remoteMessage.data["senderName"]
            val senderUid = remoteMessage.data["senderUid"]

            // Log data payload
            Log.d("FCM", "Receiver UID: $receiverUid")
            Log.d("FCM", "Receiver Name: $receiverName")
            Log.d("FCM", "Message: $message")
            Log.d("FCM", "Sender Name: $senderName")
            Log.d("FCM", "Sender Uid: $senderUid")

            // Get the currently open chat user ID
            prefs = UserSessionManager(this)
            val currentChatUserId = prefs.notificationSenderUid
            if (currentChatUserId == senderUid){
                return
            }else{
                senderUid?.let { prefs.addUnreadUser(it) }
                val intent = Intent("update_user_list")
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                // Send notification with an intent to open ChatActivity
                showNotification(senderName,receiverUid,receiverName, message,senderUid)
            }
        }
    }

    private fun showNotification(senderName: String?, receiverUid: String?, receiverName: String?, message: String?,senderUid : String?) {
        // Create an intent for opening ChatActivity when notification is clicked
        val intent = Intent(this, ChatActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("receiverUid", receiverUid)
            putExtra("receiverName", receiverName)
            putExtra("message", message)
            putExtra("senderName", senderName)
            putExtra("senderUid",senderUid)

        }
         pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val channelId = "chat_message_channel"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.cropedicon)
            .setContentTitle("New message from $senderName")
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // For Android Oreo and above, create a notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Chat Message Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }
}