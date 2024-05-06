package com.test.loginfirebase

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.test.loginfirebase.adapter.MessageAdapter
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
    private var isKeyboardOpen = false

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

        //Back button
        imageBack.setOnClickListener {
            finish()
        }


        databaseReference = FirebaseDatabase.getInstance().getReference()

        val receiverUid = intent.getStringExtra("uid")
        val senderUid = FirebaseAuth.getInstance().currentUser?.uid
        val name = intent.getStringExtra("name")
        val text = findViewById<TextView>(R.id.toolbar).apply {
            text = name
        }
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
                        messageList.add(message!!)
                    }
                    progressBar.visibility = View.GONE
                    adapter.notifyDataSetChanged()


                    recyclerView.post{
                        recyclerView.smoothScrollToPosition(adapter.itemCount - 0)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

        binding.root.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
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


// message sent button
        imageView.setOnClickListener {

            val text = editText.text.toString().trim()
            val messageObject = Message(text, senderUid)

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


        }

    }
    private fun scrollToBottom() {
        recyclerView.scrollToPosition(adapter.itemCount - 1)
    }
    private fun listenForOnlineStatus(receiverUid: String?) {
        val receiverUserRef = FirebaseDatabase.getInstance().getReference().child("User").child(receiverUid!!)
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
}
