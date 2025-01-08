package com.test.loginfirebase.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.text.InputFilter
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.stfalcon.imageviewer.StfalconImageViewer
import com.test.loginfirebase.ChatActivity
import com.test.loginfirebase.R
import com.test.loginfirebase.data.Message

import com.test.loginfirebase.data.User
import com.test.loginfirebase.utils.FirebaseUtil
import com.test.loginfirebase.utils.sessionManager.UserSessionManager
import de.hdodenhof.circleimageview.CircleImageView
import java.util.Calendar

class UserAdapter(
    val context: Context,
    private var filteredList: ArrayList<User>
) : RecyclerView.Adapter<UserAdapter.UserViewholder>() {
    private val databaseReference: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().getReference("Users")
    }
    private val prefs: UserSessionManager by lazy {
        UserSessionManager(context)
    }
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private var currentUserId: String? = null
    var senderRoom: String? = null
    var receiverRoom: String? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewholder {

        currentUserId = firebaseAuth.currentUser?.uid

        val view = LayoutInflater.from(context).inflate(R.layout.user_layout, parent, false)
        return UserViewholder(context, view)

    }

    override fun getItemCount(): Int {

        return filteredList.size
    }

    override fun onBindViewHolder(holder: UserViewholder, position: Int) {
        val users = filteredList[position]
        holder.text.text = users.name


        // Fetch and display the profile image of the current user in the list
        users.uid?.let {
            databaseReference.child(it).child("profileImageUrl")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val imageUrl = snapshot.value as? String
                        imageUrl?.let {
                            Glide.with(context)
                                .load(it)
                                .placeholder(R.drawable.portrait_placeholder)
                                .error(R.drawable.portrait_placeholder)
                                // Ensure it's not loading from cache
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(holder.userImage)  // Load updated image
                            // Update the currentUser object with the image URL
                            users.profileImageUrl = it
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e(
                            "UserAdapter",
                            "Failed to listen for image URL changes: ${error.message}"
                        )
                    }
                })
        }

        holder.userImage.setOnClickListener {
            showImageDialog(users.profileImageUrl)

        }


        val userStatusRef =
            FirebaseDatabase.getInstance().getReference("Users").child(users.uid!!)
                .child("status")

        userStatusRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.getValue(String::class.java)
                Log.d("Check status", status.toString())
                if (status == "online") {
                    // Show "Online" below the name
                    holder.onlineDotView.visibility = View.VISIBLE

                } else {
                    // Show "Offline" below the name or hide the status
                    holder.onlineDotView.visibility = View.GONE

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Check status", error.toString())
            }
        })


        holder.bind(users)

        holder.itemView.setOnClickListener {
            val savedPin = prefs.getChatPin(users.uid!!)

            if (!savedPin.isNullOrEmpty()) {
                // If PIN is set, show the dialog to enter the PIN
                showEnterPinDialog(context, users) {
                    // On successful PIN validation, open the chat
                    openChat(context, users)
                }
            } else {
                // If no PIN is set, directly open the chat
                openChat(context, users)
            }

            /* val intent = Intent(context, ChatActivity::class.java)

             intent.putExtra("name", users.name)
             intent.putExtra("uid", users.uid)
             intent.putExtra("imageUrl", users.profileImageUrl)

             context.startActivity(intent)*/
        }

        holder.checkUserStories(users.uid)

        senderRoom = users.uid + currentUserId
        receiverRoom = currentUserId + users.uid

        fetchLastMessage(
            senderRoom!!,
            users.uid!!
        ) { lastMessage, lastMessageTime, lastDate, isFromSender ->

            holder.lastMessage.text = if (isFromSender) "You : $lastMessage" else lastMessage
            holder.lastMessageTime.text = lastMessageTime
            holder.lastMessageDate.text = lastDate

        }
    }

    class UserViewholder(context: Context, itemview: View) : RecyclerView.ViewHolder(itemview) {

        private val prefs: UserSessionManager by lazy {
            UserSessionManager(context)
        }

        val text = itemview.findViewById<TextView>(R.id.txtName)!!
        var userImage = itemview.findViewById<CircleImageView>(R.id.imageProfile)!!
        private val greenDotView: View = itemView.findViewById(R.id.greenDotView)
        val onlineDotView: View = itemView.findViewById(R.id.onlineDotView)
        val lastMessage = itemView.findViewById<TextView>(R.id.lastmessage)!!
        val lastMessageTime = itemView.findViewById<TextView>(R.id.lastmessagetime)!!
        val lastMessageDate = itemView.findViewById<TextView>(R.id.lastmessageDate)!!

        // val you = itemView.findViewById<TextView>(R.id.you)!!
        val activeStory = itemView.findViewById<View>(R.id.activeStory)!!


        fun bind(user: User) {
            // Check if this user has an unread message and show the green dot if true
            val unreadUsers = prefs.getUnreadUsers()
            if (unreadUsers.contains(user.uid)) {
                greenDotView.visibility = View.VISIBLE
            } else {
                greenDotView.visibility = View.GONE
            }
        }

        // Method to check if the user has stories
        fun checkUserStories(userId: String?) {
            if (userId != null) {
                val databaseReference =
                    FirebaseDatabase.getInstance().getReference("Users").child(userId)
                        .child("story")
                databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        // Check if the user has stories
                        if (snapshot.exists() && snapshot.childrenCount > 0) {
                            // User has stories, make the activeStory view visible
                            activeStory.visibility = View.VISIBLE
                        } else {
                            // User has no stories, hide the activeStory view
                            activeStory.visibility = View.GONE
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("UserAdapter", "Failed to check stories: ${error.message}")
                    }
                })
            } else {
                activeStory.visibility = View.GONE // Hide if no user ID
            }
        }
    }

    private fun showImageDialog(imageUrl: String?) {
        StfalconImageViewer.Builder(context, listOf(imageUrl)) { view, image ->
            // Load the image into the viewer using Glide
            Glide.with(context)
                .load(image)
                .placeholder(R.drawable.portrait_placeholder)
                .error(R.drawable.portrait_placeholder)
                .into(view)
        }.show()
    }

    fun filterList(filteredList: ArrayList<User>) {
        this.filteredList = filteredList
        // Sort the list based on lastMessageTimestamp in descending order
        this.filteredList.sortByDescending { it.lastChatTimestamp }
        notifyDataSetChanged()
    }

    // Function to fetch last message
    private fun fetchLastMessage(
        senderRoom: String,
        receiverUid: String,
        callback: (String, String, String, Boolean) -> Unit
    ) {
        val lastMessageRef =
            FirebaseDatabase.getInstance().getReference("chats").child(senderRoom).child("messages")

        // Query the last message
        lastMessageRef.orderByKey().limitToLast(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var lastMessage = ""
                    var formattedTime = ""
                    var formattedDate = ""
                    var isFromSender = false
                    if (snapshot.exists()) {
                        for (messageSnapshot in snapshot.children) {
                            val messageObject = messageSnapshot.getValue(Message::class.java)
                            lastMessage = messageObject?.message ?: "No messages yet"
                            val timeStamp = messageObject?.timeStamp ?: System.currentTimeMillis()
                            val dateStamp = messageObject?.dateStamp ?: System.currentTimeMillis()

                            // Update the last message timestamp for the user
                            if (messageObject != null) {
                                filteredList.find { it.uid == receiverUid }?.lastChatTimestamp =
                                    timeStamp
                            }
                            // Format the timestamp to a readable time form
                            formattedTime = formatTimestamp(timeStamp)
                            // Check if the date is today

                            formattedDate = when {
                                isToday(dateStamp) -> "" // Don't show date if it's today
                                isYesterday(dateStamp) -> "Yesterday" // Show "Yesterday" if it's yesterday
                                else -> formatDatestamp(dateStamp) // Show the formatted date otherwise
                            }
                            isFromSender =
                                messageObject?.senderId == FirebaseUtil().currentUserId()

                        }
                    }
                    callback(lastMessage, formattedTime, formattedDate, isFromSender)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Fetch Last Message", "Failed to fetch last message: ${error.message}")
                    callback("Error loading message", "", "", false)
                }
            })
    }

    private fun formatTimestamp(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat(
            "hh:mm a",
            java.util.Locale.getDefault()
        ) // Or use another format like "dd MMM, HH:mm"
        val date = java.util.Date(timestamp)
        return sdf.format(date)
    }

    private fun formatDatestamp(dateStsmp: Long): String {
        val sdf = java.text.SimpleDateFormat(
            "dd/MM/yyyy",
            java.util.Locale.getDefault()
        ) // Or use another format like "dd MMM, HH:mm"
        val date = java.util.Date(dateStsmp)
        return sdf.format(date)
    }

    fun updateLastMessage(receiverUid: String) {
        val index = filteredList.indexOfFirst { it.uid == receiverUid }
        if (index != -1) {
            // Update the last message for this user
            notifyItemChanged(index)
        }
    }

    private fun isToday(timestamp: Long): Boolean {
        val todayStart = getStartOfDay(System.currentTimeMillis())
        return timestamp >= todayStart
    }

    private fun isYesterday(timestamp: Long): Boolean {
        val yesterdayStart = getStartOfDay(System.currentTimeMillis() - 24 * 60 * 60 * 1000)
        val todayStart = getStartOfDay(System.currentTimeMillis())
        return timestamp in yesterdayStart until todayStart
    }

    private fun getStartOfDay(timeInMillis: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun showEnterPinDialog(context: Context, users: User, onSuccess: () -> Unit) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Enter PIN")
        builder.setMessage("This chat is protected by a PIN.")

        val input = EditText(context)
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        input.hint = "Enter your PIN"
        input.filters = arrayOf(InputFilter.LengthFilter(4)) // Restrict to 4 digits
        builder.setView(input)

        builder.setPositiveButton("Unlock") { dialog, _ ->
            val enteredPin = input.text.toString()
            val savedPin =
                prefs.getChatPin(users.uid!!) // Replace with your session manager's `getChatPin()` method
            if (enteredPin == savedPin) {
                onSuccess() // Call the success callback if PIN is correct
            } else {
                Toast.makeText(context, "Incorrect PIN!", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNeutralButton("Steal PIN") { dialog, _ ->
            warningDialog(users)
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun openChat(context: Context, users: User) {
        val intent = Intent(context, ChatActivity::class.java)
        intent.putExtra("name", users.name)
        intent.putExtra("uid", users.uid)
        intent.putExtra("imageUrl", users.profileImageUrl)
        context.startActivity(intent)
    }

    private fun warningDialog(users: User) {
        androidx.appcompat.app.AlertDialog.Builder(context)
            .setTitle("Alert!")
            .setMessage("This will clear all messages\nThe secret pin, if set, will also be cleared")
            .setPositiveButton("OK") { _, _ ->
                senderRoom = users.uid + currentUserId
                deleteAllMessagesForUser(users, senderRoom!!) {
                    openChat(context, users)
                }

            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteAllMessagesForUser(users: User, senderRoom: String, onComplete: () -> Unit) {
        val database = FirebaseDatabase.getInstance()
        val messagesRef = database.getReference("chats").child(senderRoom).child("messages")

        // Use Firebase to delete all messages
        messagesRef.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                prefs.clearChatPin(users.uid!!)
                FirebaseDatabase.getInstance().getReference().child("Users")
                    .child(FirebaseUtil().currentUserId()!!).child("Secret PIN")
                    .child(users.name.toString()).removeValue()
                Toast.makeText(context, "All messages deleted!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to delete messages", Toast.LENGTH_SHORT).show()
            }
            onComplete() // Call the callback to proceed with opening the chat
        }
    }

}


