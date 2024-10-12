package com.test.loginfirebase

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Rect
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.AlertDialog
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
import com.google.firebase.storage.FirebaseStorage
import com.test.loginfirebase.adapter.MessageAdapter
import com.test.loginfirebase.adapter.UserAdapter
import com.test.loginfirebase.data.Message
import com.test.loginfirebase.databinding.ActivityChatBinding
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var imageView: CircleImageView
    private lateinit var editText: EditText
    private lateinit var adapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>
    lateinit var databaseReference: DatabaseReference
    lateinit var imageBack: ImageView
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var addicon: ImageView
    private var isKeyboardOpen = false
    private val REQUEST_CODE_SELECT_IMAGE = 1
    private var isAddIconRotated = false
    private lateinit var rotationAnimator: ObjectAnimator

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


        databaseReference = FirebaseDatabase.getInstance().getReference()

        val receiverUid = intent.getStringExtra("uid")
        val senderUid = FirebaseAuth.getInstance().currentUser?.uid
        val name = intent.getStringExtra("name")
        val imageUrl = intent.getStringExtra("imageUrl")
        val text = findViewById<TextView>(R.id.toolbarName).apply {
            text = name
        }

            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.portrait_placeholder)
                .error(R.drawable.portrait_placeholder)
                .into(binding.image) // Ensure this is the correct image view you're using

        listenForOnlineStatus(receiverUid)

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

                        val message = postss.getValue(Message::class.java)
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
            val messageObject = Message(text, senderUid)

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


    private fun showDeleteDialog(message: Message) {
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

    private fun deleteMessage(message: Message) {
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
        val receiverUserRef =
            FirebaseDatabase.getInstance().getReference().child("User").child(receiverUid!!)
        receiverUserRef.child("online").addValueEventListener(object : ValueEventListener {
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




