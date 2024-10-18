package com.test.loginfirebase

import android.animation.ObjectAnimator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Rect
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.test.loginfirebase.adapter.MessageAdapter
import com.test.loginfirebase.apiInterface.NotificationApiService
import com.test.loginfirebase.data.model.Messagee
import com.test.loginfirebase.data.model.Notification
import com.test.loginfirebase.data.model.NotificationRequest
import com.test.loginfirebase.databinding.ActivityChatBinding
import com.test.loginfirebase.fcm.ServerToken
import com.test.loginfirebase.fcm.getReceiverFcmToken
import com.test.loginfirebase.utils.sessionManager.UserSessionManager
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.test.loginfirebase.data.Message as Message1

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var imageView: CircleImageView
    private lateinit var editText: EditText
    private lateinit var adapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message1>
    lateinit var databaseReference: DatabaseReference
    lateinit var imageBack: ImageView
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var prefs: UserSessionManager
    private lateinit var addicon: ImageView
    private var isKeyboardOpen = false
    private val REQUEST_CODE_SELECT_IMAGE = 1
    private var isAddIconRotated = false
    private lateinit var rotationAnimator: ObjectAnimator
    private lateinit var realAccessToken: String
    private var receiverUid: String? = null  // Store userId


    var senderRoom: String? = null
    var receiverRoom: String? = null

    private val messageSentReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "MESSAGE_SENT") {
                // Update user list or move the corresponding user to the top
                val receiverUid = intent.getStringExtra("receiverUid")
                // Implement logic to update user list in the main activity
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the broadcast receiver
        unregisterReceiver(messageSentReceiver)
        prefs.removeNotificationSenderUid()
        prefs.markUserAsRead(receiverUid!!)  // Remove the user from unread list
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imageBack = findViewById(R.id.imageBack)
        recyclerView = findViewById(R.id.recyclerChat)
        imageView = findViewById(R.id.sendMessageBtn)
        editText = findViewById(R.id.typeMessage)
        addicon = findViewById(R.id.addIcon)
        mediaPlayer = MediaPlayer.create(this, R.raw.message_sound)
        val progressBar: ProgressBar = findViewById(R.id.progressBar)
        // Find the TextViews in your layout
        val textViewDay = findViewById<TextView>(R.id.textViewDay)
        val textViewDate = findViewById<TextView>(R.id.textViewDate)

        //initialize shared preference
        prefs = UserSessionManager(this)
        // Using a Coroutine to call the network operation
        lifecycleScope.launch {
            try {
                val serverToken = ServerToken(this@ChatActivity)
                realAccessToken = serverToken.getServerToken()

                Log.d("Server Token", realAccessToken!!)
            } catch (e: Exception) {
                Log.e("Error", "Failed to get server token: ${e.message}")
            }
        }

        val filter = IntentFilter("MESSAGE_SENT")
        registerReceiver(messageSentReceiver, filter)

        // Get the current day and date
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        val date = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())

        // Set the day and date to the TextViews
        textViewDay.text = dateFormat.format(calendar.time)
        textViewDate.text = date.format(calendar.time)

        rotationAnimator = ObjectAnimator.ofFloat(addicon, View.ROTATION, 0f, 45f).apply {
            duration = 200 // Animation duration in milliseconds
            interpolator =
                AccelerateDecelerateInterpolator() // Smoothly accelerate and decelerate the animation

        }

        imageBack.setOnClickListener {
            finish()
        }

        addicon.setOnClickListener {
            if (!rotationAnimator.isRunning) {
                if (isAddIconRotated) {
                    rotationAnimator.setFloatValues(45f, 0f)
                } else {
                    rotationAnimator.setFloatValues(0f, 45f)
                }
                rotationAnimator.start()
                isAddIconRotated = !isAddIconRotated
            }
            showPopupMenu(it)
        }

        // Retrieve data from the intent
        val receiverUidFromPendingIntent = intent.getStringExtra("receiverUid")
        val receiverName = intent.getStringExtra("receiverName")
        val message = intent.getStringExtra("message")
        val senderName = intent.getStringExtra("senderName")
        val senderUidd = intent.getStringExtra("senderUid")



        // Log the received data
        Log.d("ChatActivity", "Receiver UID: $receiverUidFromPendingIntent")
        Log.d("ChatActivity", "Receiver Name: $receiverName")
        Log.d("ChatActivity", "Message: $message")
        Log.d("ChatActivity", "Sender Name: $senderName")
        Log.d("ChatActivity", "Sender Uid: $senderUidd")


        val extras = intent.extras
        extras?.keySet()?.forEach { key ->
            Log.d("ChatActivity Extra", "Key: $key, Value: ${extras.get(key)}")
        }


        databaseReference = FirebaseDatabase.getInstance().getReference()

         receiverUid = intent.getStringExtra("uid")
        val senderUid = FirebaseAuth.getInstance().currentUser?.uid
        val name = intent.getStringExtra("name")
        val imageUrl = intent.getStringExtra("imageUrl")
        val text = findViewById<TextView>(R.id.toolbarName).apply {
            text = name
        }

        prefs.notificationSenderUid = receiverUid
        Log.d("check receiver Uid", "Receiver Uid: ${prefs.notificationSenderUid}")


        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.portrait_placeholder)
            .error(R.drawable.portrait_placeholder)
            .into(binding.image) // Ensure this is the correct image view you're using

        //listenForOnlineStatus(receiverUid)

        senderRoom = receiverUid + senderUid
        receiverRoom = senderUid + receiverUid


        messageList = ArrayList()
        adapter = MessageAdapter(this, messageList)


        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        databaseReference.child("chats")
            .child(senderRoom!!)
            .child("messages").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    progressBar.visibility = View.VISIBLE
                    messageList.clear()

                    for (postss in snapshot.children) {

                        val message = postss.getValue(Message1::class.java)
                        message?.messageId = postss.key ?: ""
                        messageList.add(message!!)
                    }
                    progressBar.visibility = View.GONE
                    adapter.notifyDataSetChanged()


                    recyclerView.post {
                        recyclerView.smoothScrollToPosition(adapter.itemCount - 0)
                    }
                    playMessageSound()
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

        binding.root.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val r = Rect()
                binding.root.getWindowVisibleDisplayFrame(r)
                val screenHeight = binding.root.rootView.height
                val keypadHeight = screenHeight - r.bottom

                if (keypadHeight > screenHeight * 0.15) { // Keyboard is open
                    if (!isKeyboardOpen) {
                        isKeyboardOpen = true
                        scrollToBottom()
                    }
                } else { // Keyboard is closed
                    isKeyboardOpen = false
                }
            }
        })


        imageView.setOnClickListener {

            val text = editText.text.toString().trim()
            val messageObject = Message1(text, senderUid)

            if (text.isNotEmpty()) {
                databaseReference.child("chats")
                    .child(senderRoom!!)
                    .child("messages").push()
                    .setValue(messageObject).addOnSuccessListener {

                        val intent = Intent("MESSAGE_SENT")
                        intent.putExtra("receiverUid", receiverUid)
                        sendBroadcast(intent)

                        databaseReference.child("chats")
                            .child(receiverRoom!!)
                            .child("messages").push()
                            .setValue(messageObject).addOnSuccessListener {
                                getReceiverFcmToken(receiverUid!!) { token ->

                                    sendNotificationToReceiver(
                                        receiverToken = token,
                                        message = text,
                                        senderName = prefs.userNameLogin!!,
                                        receiverUid = receiverUid!!,
                                        receiverName = name!!,
                                        senderUid = senderUid!!
                                    )
                                    println(senderUid.toString())
                                }
                            }
                    }
                editText.text.clear()
            } else {
                Toast.makeText(this, "Enter message", Toast.LENGTH_SHORT).show()
            }
        }
        adapter.onMessageLongClickListener = { message ->
            // Show a dialog when a message is long-clicked
            showDeleteDialog(message)
        }

    }

    private fun initializeChat(receiverUid: String, name: String, message: String) {
        // Initialize chat with the provided receiverUid and name
        // You can display the message or update the UI accordingly
        // For example, display the message in the chat list
        val messageObject = Message1(message, FirebaseAuth.getInstance().currentUser?.uid)
        messageList.add(messageObject) // Add the message to the list
        adapter.notifyDataSetChanged() // Update the adapter
        scrollToBottom() // Scroll to the bottom of the chat
    }


    private fun sendNotificationToReceiver(
        receiverToken: String,
        message: String,
        senderName: String,
        receiverUid: String,
        receiverName: String,
        senderUid: String
    ) {
        val notification = Notification(
            title = senderName,
            body = message,
        )

        // Create a data payload with senderUid
        val dataPayload = mapOf(
            "receiverUid" to receiverUid,
            "receiverName" to receiverName,
            "message" to message,
            "senderName" to senderName,
            "senderUid" to senderUid,

        )

        val messageObj = Messagee(
            token = receiverToken,
            notification = notification,
            data = dataPayload as Map<String, String>
        )

        val notificationRequest = NotificationRequest(message = messageObj)

        val apiService = Retrofit.Builder()
            .baseUrl("https://fcm.googleapis.com/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NotificationApiService::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response =
                    apiService.sendNotification("Bearer $realAccessToken", notificationRequest)
                if (response.isSuccessful) {
                    // Notification sent successfully
                    Log.d("Notification Response", response.body().toString())

                    runOnUiThread {
                        Toast.makeText(this@ChatActivity, "Notification sent", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Log.e("Notification Error", response.errorBody()?.string() ?: "Unknown error")
                    // Handle error
                    runOnUiThread {
                        Toast.makeText(
                            this@ChatActivity,
                            "Failed to send notification",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun resetAddIconRotation() {
        addicon.rotation = 0f
        isAddIconRotated = false
    }

    private fun rotateAddIcon() {
        if (!isAddIconRotated) {
            // Rotate the add icon by 45 degrees
            addicon.rotation = 45f
            isAddIconRotated = true
        } else {
            // Reset add icon rotation to its original position
            addicon.rotation = 0f
            isAddIconRotated = false
        }

    }


    private fun showDeleteDialog(message: Message1) {
        AlertDialog.Builder(this)
            .setTitle("Delete Message")
            .setMessage("Are you sure you want to delete this message?")
            .setPositiveButton("Delete") { _, _ ->
                // Delete the message
                deleteMessage(message)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteMessage(message: Message1) {
        val messageRef = databaseReference.child("chats").child(senderRoom!!)
            .child("messages").child(message.messageId)

        messageRef.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                messageList.remove(message)
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "Message deleted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to delete message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun scrollToBottom() {
        recyclerView.scrollToPosition(adapter.itemCount - 1)
    }

    private fun listenForOnlineStatus(receiverUid: String?) {
        if (receiverUid == null) {
            Log.d("receiver uid check","receiver uid is NULL")

        }
        val receiverUserRef =
            receiverUid?.let { FirebaseDatabase.getInstance().getReference().child("User").child(it) }
        receiverUserRef?.child("online")?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isOnline = snapshot.getValue(Boolean::class.java) ?: false

                if (isOnline) {

                    val textViewOnlineStatus = findViewById<TextView>(R.id.onlineStatus)
                    textViewOnlineStatus.text = "Online"
                } else {
                    val textViewOnlineStatus = findViewById<TextView>(R.id.onlineStatus)
                    textViewOnlineStatus.text = "Offline"
                    /*if (lastSeenTimestamp != null) {
                    val lastSeenTime = getTimeAgo(lastSeenTimestamp)
                    textViewOnlineStatus.text = "Last seen $lastSeenTime"
                } else {
                    textViewOnlineStatus.text = "Offline"
                }*/
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled
            }
        })
    }

    private fun playMessageSound() {
        // Check if MediaPlayer is initialized and not playing
        mediaPlayer.let { player ->
            if (!player.isPlaying) {
                player.start()
            }
        }
    }

    private fun getTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val timeDifference = now - timestamp

        val SECOND_MILLIS: Long = 1000
        val MINUTE_MILLIS = 60 * SECOND_MILLIS
        val HOUR_MILLIS = 60 * MINUTE_MILLIS
        val DAY_MILLIS = 24 * HOUR_MILLIS

        return when {
            timeDifference < MINUTE_MILLIS -> "just now"
            timeDifference < 2 * MINUTE_MILLIS -> "a minute ago"
            timeDifference < 50 * MINUTE_MILLIS -> "${timeDifference / MINUTE_MILLIS} minutes ago"
            timeDifference < 90 * MINUTE_MILLIS -> "an hour ago"
            timeDifference < 24 * HOUR_MILLIS -> "${timeDifference / HOUR_MILLIS} hours ago"
            timeDifference < 48 * HOUR_MILLIS -> "yesterday"
            else -> "${timeDifference / DAY_MILLIS} days ago"
        }
    }


    override fun onResume() {
        super.onResume()
        // Your existing code...
    }

    override fun onPause() {
        super.onPause()
        // Your existing code...
    }

    override fun onStop() {
        super.onStop()
        // Your existing code...
    }

    fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.popmenuforimg, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.camera -> {
                    // Handle menu item 1 click
                    val intent = Intent(Intent.ACTION_PICK)
                    intent.type = "image/*"
                    startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE)
                    true
                }

                R.id.gallery -> {
                    // Handle menu item 2 click
                    true
                }

                R.id.mic -> {
                    // Handle menu item 2 click
                    true
                }

                else -> false
            }
        }
        // Use custom style to position the popup menu at the bottom of the screen
        //   popupMenu.setStyle(R.style.PopupMenuBottomStyle)
        popupMenu.show()
    }
}




