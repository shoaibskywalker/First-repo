package com.test.loginfirebase.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.test.loginfirebase.ChatActivity
import com.test.loginfirebase.R

import com.test.loginfirebase.data.User
import com.test.loginfirebase.utils.sessionManager.UserSessionManager
import de.hdodenhof.circleimageview.CircleImageView

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


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewholder {

        currentUserId = firebaseAuth.currentUser?.uid

        val view = LayoutInflater.from(context).inflate(R.layout.user_layout, parent, false)
        return UserViewholder(context,view)

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

holder.bind(currentUser)

        holder.itemView.setOnClickListener {

            val intent = Intent(context, ChatActivity::class.java)

            intent.putExtra("name", currentUser.name)
            intent.putExtra("uid", currentUser.uid)
            intent.putExtra("imageUrl", currentUser.profileImageUrl)

            context.startActivity(intent)
        }
    }

    class UserViewholder(context: Context,itemview: View) : RecyclerView.ViewHolder(itemview) {

        private val prefs: UserSessionManager by lazy {
            UserSessionManager(context)
        }

        val text = itemview.findViewById<TextView>(R.id.txtName)
        var userImage = itemview.findViewById<CircleImageView>(R.id.imageProfile)
        private val greenDotView: View = itemView.findViewById(R.id.greenDotView)


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

    fun filterList(filteredList: ArrayList<User>) {
        this.filteredList = filteredList
        notifyDataSetChanged()
    }

}


