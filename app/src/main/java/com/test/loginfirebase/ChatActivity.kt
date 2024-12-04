package com.test.loginfirebase

import android.animation.ObjectAnimator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.test.loginfirebase.adapter.MessageAdapter
import com.test.loginfirebase.apiInterface.NotificationApiService
import com.test.loginfirebase.data.model.Messagee
import com.test.loginfirebase.data.model.Notification
import com.test.loginfirebase.data.model.NotificationRequest
import com.test.loginfirebase.databinding.ActivityChatBinding
import com.test.loginfirebase.fcm.ServerToken
import com.test.loginfirebase.fcm.getReceiverFcmToken
import com.test.loginfirebase.utils.CommonUtil
import com.test.loginfirebase.utils.FirebaseUtil
import com.test.loginfirebase.utils.sessionManager.UserSessionManager
import com.zegocloud.uikit.service.defines.ZegoUIKitUser
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
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
    private lateinit var prefs: UserSessionManager
    private lateinit var addicon: ImageView
    private var isKeyboardOpen = false
    private var isAddIconRotated = false
    private lateinit var rotationAnimator: ObjectAnimator
    private lateinit var realAccessToken: String
    private var receiverUid: String? = null  // Store userId
    private var about: String? = null
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>


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
        val progressBar: ProgressBar = findViewById(R.id.progressBar)

        //initialize shared preference
        prefs = UserSessionManager(this)
        // Using a Coroutine to call the network operation
        lifecycleScope.launch {
            try {
                val serverToken = ServerToken(this@ChatActivity)
                realAccessToken = serverToken.getServerToken()

                Log.d("Server Token", realAccessToken)
            } catch (e: Exception) {
                Log.e("Error", "Failed to get server token: ${e.message}")
            }
        }

        val intentFilter = IntentFilter("MESSAGE_SENT")
        registerReceiver(messageSentReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED)
        // registerReceiver(messageSentReceiver, filter)

        // Get the current day and date
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        val date = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())

        rotationAnimator = ObjectAnimator.ofFloat(addicon, View.ROTATION, 0f, 45f).apply {
            duration = 200 // Animation duration in milliseconds
            interpolator =
                AccelerateDecelerateInterpolator() // Smoothly accelerate and decelerate the animation

        }

        imageBack.setOnClickListener {
            finish()
        }

        pickImageLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val imageUri: Uri? = result.data!!.data
                imageUri?.let { uri ->
                    // Using MediaStore to access the image
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                    binding.image.setImageBitmap(bitmap)  // Display in ImageView
                    Log.d("selected image", uri.toString())
                    // Save the selected image to SharedPreferences
                    // Save the selected image for the current user
                    /*currentUserEmail?.let { email ->
                        userSessionManager.saveUserProfileImage(email, bitmap)
                    }*/

                }
            }
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
            showPopupMenu(launcher = pickImageLauncher,it)
            binding.cancel.setOnClickListener{
                binding.entermessage.visibility = View.VISIBLE
                binding.sendMessageBtn.visibility = View.VISIBLE
                binding.linearAudio.visibility = View.GONE
            }
        }


        databaseReference = FirebaseDatabase.getInstance().getReference()

        receiverUid = intent.getStringExtra("uid")
        val senderUid = FirebaseUtil().currentUserId()
        val name = intent.getStringExtra("name")
        val imageUrl = intent.getStringExtra("imageUrl")
        val text = findViewById<TextView>(R.id.toolbarName).apply {
            text = name
        }
        binding.threedoticon.setOnClickListener {
            showChatLockMenu(it,name)
        }

                databaseReference.child("Users").child(receiverUid!!).child("About").get()
                    .addOnSuccessListener { snapshot ->
                        val aboutFetch = if (snapshot.exists())snapshot.value as? String else "$name did not add any about!"
                          about= aboutFetch
                        Log.d("about check",about.toString())

                    }.addOnFailureListener {
                        Log.d("about",about.toString())
                    }


        binding.toolbarName.setOnClickListener{
            showUserDetailDialog(imageUrl = imageUrl, username = name!!, userabout = about!!)
        }

        binding.voiceCall.setIsVideoCall(false)
        binding.voiceCall.setResourceID("zego_uikit_call")
        binding.voiceCall.setInvitees(listOf(ZegoUIKitUser(receiverUid)))

        binding.videoCall.setIsVideoCall(true)
        binding.videoCall.setResourceID("zego_uikit_call")
        binding.videoCall.setInvitees(listOf(ZegoUIKitUser(receiverUid)))

        prefs.notificationSenderUid = receiverUid

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
                    // playMessageSound()
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
                CommonUtil.showToastMessage(this, "Enter message")
            }
        }
        adapter.onMessageLongClickListener = { message ->
            // Show a dialog when a message is long-clicked
            showDeleteDialog(message)
        }

        val uid = FirebaseUtil().currentUserId()
        val userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid!!)

        // Set last online timestamp on disconnect
        userRef.child("lastOnline").onDisconnect().setValue(ServerValue.TIMESTAMP)

        // for online status
        val userStatusRef =
            FirebaseDatabase.getInstance().getReference("Users").child(receiverUid!!)
                .child("status")

        userStatusRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val status = snapshot.getValue(String::class.java)
                    Log.d("Check status", status.toString())
                    if (status == "offline") {
                        FirebaseDatabase.getInstance().getReference("Users").child(receiverUid!!)
                            .child("lastOnline")
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(lastOnlineSnapshot: DataSnapshot) {
                                    val lastOnlineTimestamp =
                                        lastOnlineSnapshot.getValue(Long::class.java)
                                    if (lastOnlineTimestamp != null) {
                                        val lastOnlineTime = formatLastSeen(lastOnlineTimestamp)
                                        binding.onlineStatus.text = lastOnlineTime

                                    } else {
                                        binding.onlineStatus.text = "Offline"
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {

                                }
                            })
                    } else {
                        // Show "Offline" below the name or hide the status
                        binding.onlineStatus.text = "Online"
                    }
                } else {

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        // for typing... status
        val receiver = FirebaseDatabase.getInstance().getReference("Users").child(receiverUid!!)
            .child("typing")
        receiver.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isTyping = snapshot.getValue(Boolean::class.java) ?: false
                if (isTyping) {
                    // Display "typing..." when the receiver is typing
                    binding.onlineStatus.text = "typing..."
                } else {
                    binding.onlineStatus.text = "online"
// If not typing, maintain the current online/offline status
                    /*userStatusRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val status = snapshot.getValue(String::class.java)
                            binding.onlineStatus.text = if (status == "offline") "Offline" else "Online"
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("ChatActivity", "Error retrieving status: ${error.message}")
                        }
                    })*/
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })


        val typingRef =
            FirebaseDatabase.getInstance().getReference("Users").child(uid).child("typing")

        val typingHandler = Handler(Looper.getMainLooper())
        val typingRunnable = Runnable {
            typingRef.setValue(false)  // Set typing status to false after inactivity
        }

        typingRef.setValue(false)

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if (s.toString().isNotEmpty()) {
                    typingRef.setValue(true)  // Set typing status to true
                    typingHandler.removeCallbacks(typingRunnable) // Remove any pending actions
                    typingHandler.postDelayed(
                        typingRunnable,
                        2000
                    ) // Post runnable to clear typing status after 5 seconds
                } else {
                    typingRef.setValue(false) // Set typing status to false when input is empty
                    typingHandler.removeCallbacks(typingRunnable) // Ensure no pending runnable is executed
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })


    }


    private fun formatTime(timestamp: Long): String {
        val dateFormat =
            SimpleDateFormat("hh:mm a", Locale.getDefault()) // 12-hour format with AM/PM
        return dateFormat.format(Date(timestamp))
    }

    // Function to format last seen time
    private fun formatLastSeen(timestamp: Long): String {
        val lastSeenDate = Date(timestamp)
        val calendar = Calendar.getInstance()
        calendar.time = lastSeenDate

        val currentCalendar = Calendar.getInstance()

        return when {
            calendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
                    calendar.get(Calendar.DAY_OF_YEAR) == currentCalendar.get(Calendar.DAY_OF_YEAR) -> {
                "Last seen today at ${formatTime(timestamp)}"
            }

            calendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
                    calendar.get(Calendar.DAY_OF_YEAR) == currentCalendar.get(Calendar.DAY_OF_YEAR) - 1 -> {
                "Last seen yesterday at ${formatTime(timestamp)}"
            }

            else -> {
                val dateFormat = SimpleDateFormat("d MMM, yyyy", Locale.getDefault())
                "Last seen on ${dateFormat.format(lastSeenDate)}"
            }
        }
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
            data = dataPayload
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

                } else {
                    Log.e("Notification Error", response.errorBody()?.string() ?: "Unknown error")
                    // Handle error
                    runOnUiThread {

                        CommonUtil.showToastMessage(
                            this@ChatActivity,
                            "Failed to send notification"
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
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
                CommonUtil.showToastMessage(this, "Message deleted")
            } else {
                CommonUtil.showToastMessage(this, "Failed to delete message")
            }
        }
    }

    private fun scrollToBottom() {
        recyclerView.scrollToPosition(adapter.itemCount - 1)
    }


    fun showPopupMenu(launcher: ActivityResultLauncher<Intent>,view: View) {

        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.popmenuforimg, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.camera -> {
                    CommonUtil.showToastMessage(this, "Coming soon...")
                    true
                }

                R.id.gallery -> {
                    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    intent.type = "image/*"
                    launcher.launch(intent)
                    true
                }

                R.id.mic -> {
                    binding.entermessage.visibility = View.GONE
                    binding.sendMessageBtn.visibility = View.GONE
                    binding.linearAudio.visibility = View.VISIBLE
                    true
                }

                else -> false
            }
        }

        popupMenu.show()
    }

    fun showChatLockMenu(view: View,name:String?) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.chatlock, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.enablechatLock -> {
                    if (prefs.getChatPin(receiverUid!!).isNullOrEmpty()) {
                        // No PIN set, ask to create a new PIN
                        showSetPinDialog(name)
                    } else {
                        CommonUtil.showToastMessage(this, "PIN already set!")
                    }
                    true
                }
                R.id.disablechatLock -> {
                    if (prefs.getChatPin(receiverUid!!).isNullOrEmpty()) {
                        CommonUtil.showToastMessage(this, "PIN is not set")

                    }else{
                        prefs.clearChatPin(receiverUid!!)
                        FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseUtil().currentUserId()!!).child("Secret PIN").child(name.toString()).removeValue()
                        CommonUtil.showToastMessage(this, "This chat is now un-locked")
                    }
                    true
                }

                else -> false
            }
        }
        popupMenu.show()
    }

    private fun showSetPinDialog(name: String?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Set PIN")

        // Add a text input field
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        input.hint = "Enter 4-digit PIN"
        input.filters = arrayOf(InputFilter.LengthFilter(4)) // Limit to 4 digits
        builder.setView(input)

        builder.setPositiveButton("Set") { dialog, _ ->
            val pin = input.text.toString()
            if (pin.length == 4) {
                prefs.setChatPin(receiverUid!!,pin)
                FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseUtil().currentUserId()!!).child("Secret PIN").child(name.toString()).setValue(pin)
                CommonUtil.showToastMessage(this, "PIN set successfully!")
            } else {
                CommonUtil.showToastMessage(this, "PIN must be 4 digits!")
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }



    private fun showUserDetailDialog(imageUrl: String?,username: String,userabout: String) {
        val dialogBuilder = android.app.AlertDialog.Builder(this)
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_user_details, null)
        dialogBuilder.setView(dialogView)

        val imageView = dialogView.findViewById<CircleImageView>(R.id.imageUserDetail)
        val userName = dialogView.findViewById<TextView>(R.id.userName)
        val userAbout = dialogView.findViewById<TextView>(R.id.about)



        // Load the image into the ImageView
        imageUrl?.let {
            Glide.with(this)
                .load(it)
                .placeholder(R.drawable.portrait_placeholder)
                .error(R.drawable.portrait_placeholder)
                .into(imageView)
        }
        if (imageUrl.isNullOrEmpty()) {

            Glide.with(this)
                .load(R.drawable.portrait_placeholder)
                .placeholder(R.drawable.portrait_placeholder)
                .error(R.drawable.portrait_placeholder)
                .into(imageView)
        }

        userName.text = username
        userAbout.text = userabout

        val dialog = dialogBuilder.create()
        dialog.show()
    }
}




