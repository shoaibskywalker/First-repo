package com.test.loginfirebase.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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
    val name: ArrayList<User>,
    private var filteredList: ArrayList<User>
) : RecyclerView.Adapter<UserAdapter.UserViewholder>() {
    private val databaseReference: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().getReference("Users")
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
        val currentUser = filteredList[position]
        holder.text.text = currentUser.name

        // Fetch and display the profile image of the current user in the list
        currentUser.uid?.let {
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
                            currentUser.profileImageUrl = it
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
            showImageDialog(currentUser.profileImageUrl)
        }


        val userStatusRef =
            FirebaseDatabase.getInstance().getReference("Users").child(currentUser.uid!!)
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


        holder.bind(currentUser)

        holder.itemView.setOnClickListener {

            val intent = Intent(context, ChatActivity::class.java)

            intent.putExtra("name", currentUser.name)
            intent.putExtra("uid", currentUser.uid)
            intent.putExtra("imageUrl", currentUser.profileImageUrl)

            context.startActivity(intent)
        }

        senderRoom = currentUser.uid + currentUserId
        receiverRoom = currentUserId + currentUser.uid

        fetchLastMessage(
            senderRoom!!,
            currentUser.uid!!
        ) { lastMessage, lastMessageTime, lastDate, isFromSender ->

            holder.lastMessage.text = lastMessage
            holder.lastMessageTime.text = lastMessageTime
            holder.lastMessageDate.text = lastDate
            if (isFromSender) {
                holder.you.visibility = View.VISIBLE
            } else {
                holder.you.visibility = View.GONE
            }
        }
    }

    class UserViewholder(context: Context, itemview: View) : RecyclerView.ViewHolder(itemview) {

        private val prefs: UserSessionManager by lazy {
            UserSessionManager(context)
        }

        val text = itemview.findViewById<TextView>(R.id.txtName)
        var userImage = itemview.findViewById<CircleImageView>(R.id.imageProfile)
        val greenDotView: View = itemView.findViewById(R.id.greenDotView)
        val onlineDotView: View = itemView.findViewById(R.id.onlineDotView)
        val lastMessage = itemView.findViewById<TextView>(R.id.lastmessage)
        val lastMessageTime = itemView.findViewById<TextView>(R.id.lastmessagetime)
        val lastMessageDate = itemView.findViewById<TextView>(R.id.lastmessageDate)
        val you = itemView.findViewById<TextView>(R.id.you)


        fun bind(user: User) {
            // Check if this user has an unread message and show the green dot if true
            val unreadUsers = prefs.getUnreadUsers(itemView.context)
            if (unreadUsers.contains(user.uid)) {
                greenDotView.visibility = View.VISIBLE
            } else {
                greenDotView.visibility = View.GONE
            }
        }
    }

    private fun showImageDialog(imageUrl: String?) {
        val dialogBuilder = AlertDialog.Builder(context)
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_image_view, null)
        dialogBuilder.setView(dialogView)

        val imageView = dialogView.findViewById<ImageView>(R.id.dialogImageView)

        // Load the image into the ImageView
        imageUrl?.let {
            Glide.with(context)
                .load(it)
                .placeholder(R.drawable.portrait_placeholder)
                .error(R.drawable.portrait_placeholder)
                .into(imageView)
        }
        if (imageUrl.isNullOrEmpty()) {

            Glide.with(context)
                .load(R.drawable.portrait_placeholder)
                .placeholder(R.drawable.portrait_placeholder)
                .error(R.drawable.portrait_placeholder)
                .into(imageView)
        }

        val dialog = dialogBuilder.create()
        dialog.show()
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

    fun updateLastMessage(receiverUid: String, lastMessage: String) {
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
}


