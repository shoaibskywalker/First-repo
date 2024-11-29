package com.test.loginfirebase.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.test.loginfirebase.data.model.Status
import com.test.loginfirebase.databinding.StatusItemBinding
import com.test.loginfirebase.utils.sessionManager.UserSessionManager
import omari.hamza.storyview.StoryView
import omari.hamza.storyview.model.MyStory
import java.util.Date

class StatusAdapter(val context: Context, var statusList: List<Status>) :
    RecyclerView.Adapter<StatusAdapter.ViewHolder>() {
    private val databaseReference: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().getReference("Users")
    }
    private var storyData: Status? = null
    private  val prefs: UserSessionManager by lazy {
        UserSessionManager(context)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding = StatusItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)

    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = statusList[position]

        holder.binding.activeStory.visibility = View.VISIBLE
// Show only the first 7 letters of the name and add "..." if it’s longer
        val displayName = if (currentItem.name.length > 7) {
            "${currentItem.name.substring(0, 7)}..."
        } else {
            currentItem.name
        }
        holder.binding.statusName.text = displayName
        Glide.with(holder.itemView.context)
            .load(currentItem.imageUrl)
            .into(holder.binding.image)


        Log.d("image", currentItem.imageUrl)
        holder.itemView.setOnClickListener {
            currentItem.userId.let { userId ->

                fetchStoriesForUser(userId)
                Log.d("StatusReciverId", currentItem.userId)

            }
        }

    }

    override fun getItemCount(): Int {
        return statusList.size
    }


    class ViewHolder(val binding: StatusItemBinding) : RecyclerView.ViewHolder(binding.root)

    // Helper function to fetch stories for a specific user
    private fun fetchStoriesForUser(userId: String) {
        val userStoryReference = databaseReference.child(userId).child("story")
        userStoryReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val storiesList = ArrayList<MyStory>()
                for (storySnapshot in snapshot.children) {
                    storyData = storySnapshot.getValue(Status::class.java)
                    // Assuming the date field exists
                    if (storyData?.imageUrl != null && storyData!!.timestamp != null) {
                        val dateConvert = Date(storyData!!.timestamp)
                        storiesList.add(MyStory(storyData!!.imageUrl, dateConvert))
                        Log.d("StatusAdapter", "Story added: ${storyData!!.imageUrl}")

                    }
                }

                // Pass the stories to the story view
                Log.d("StatusAdapter", "Total stories fetched: ${storiesList.size}")
                try {
                    showStories(storiesList, storyData!!.name,userId)
                } catch (e: Exception) {
                    Log.e("Tag", e.message.toString())
                }
                Log.e("StatusAdapter", "Successfully fetch stories")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("StatusAdapter", "Failed to fetch stories: ${error.message}")
            }
        })
    }

    private fun showStories(storiesList: ArrayList<MyStory>, receiverName: String,receiverUid: String) {
        Log.e("StatusAdapter", "Show story")
        if (storiesList.isNotEmpty()) {
            if (context is AppCompatActivity) {
                StoryView.Builder(context.supportFragmentManager)
                    .setStoriesList(storiesList)
                    .setStoryDuration(5000)
                    .setTitleText(receiverName) // You can set the user's name here
                    .setSubtitleText("") // Optional subtitle
                    .setTitleLogoUrl(prefs.getReceiverProfilePictureUrl(receiverUid))
                    .build()
                    .show()
            } else {
                Log.e("StatusAdapter", "Context is not AppCompatActivity")
            }
        } else {
            Toast.makeText(context, "No stories to display", Toast.LENGTH_SHORT).show()
        }
    }
}
