package com.test.loginfirebase

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.test.loginfirebase.adapter.AiChatAdapter
import com.test.loginfirebase.data.AiMessage
import com.test.loginfirebase.databinding.ActivityAiChatBinding
import com.test.loginfirebase.utils.CommonUtil
import com.test.loginfirebase.utils.FirebaseUtil
import com.test.loginfirebase.utils.sessionManager.UserSessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AiChat : AppCompatActivity() {

    private lateinit var binding: ActivityAiChatBinding
    private lateinit var chatRef: DatabaseReference
    private lateinit var chatAdapter: AiChatAdapter
    private lateinit var prefs: UserSessionManager
    private val messageList = ArrayList<AiMessage>()
    var API_KEY = "AIzaSyDooL54T34k0lBf4vcmOTHQRDCAoSCN91s"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAiChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = UserSessionManager(this)

        // Firebase reference to store chats
        val currentUserName = prefs.userNameLogin
        val currentUid = FirebaseUtil().currentUserId()
        chatRef = FirebaseDatabase.getInstance().getReference("AiChats").child(currentUserName!!)
            .child(currentUid!!).child("messages")


        chatAdapter = AiChatAdapter(messageList)
        binding.recyclerChat.adapter = chatAdapter
        binding.recyclerChat.layoutManager = LinearLayoutManager(this)

        binding.onlineStatus.text = "Gemini 0.2.0"
        binding.imageBack.setOnClickListener {
            finish()
        }

        loadMessagesFromFirebase()
        binding.deleteImage.setOnClickListener {

            showDeleteDialog(currentUserName = currentUserName, currentUid = currentUid)
        }
        binding.sendMessageBtn.setOnClickListener {
            val userMessage = binding.typeMessage.text.toString().trim()

            if (userMessage.isNotEmpty()) {

                addMessageToChat(userMessage, isUser = true)
                callGeminiApi(userMessage)

            } else {
               // Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
                CommonUtil.showToastMessage(this,"Please enter a message")
            }

            binding.typeMessage.text.clear()
        }
    }

    private fun callGeminiApi(prompt: String) {

        binding.progressBar.visibility = ProgressBar.VISIBLE

        val generativeModel = GenerativeModel(
            modelName = "gemini-pro",
            apiKey = API_KEY
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = generativeModel.generateContent(prompt)
                withContext(Dispatchers.Main) {
                    addMessageToChat(response.text ?: "No response", isUser = false)
                    Log.d("AI says :-", response.text.toString())
                    binding.progressBar.visibility = ProgressBar.GONE

                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = ProgressBar.GONE

                   // Toast.makeText(this@AiChat, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    CommonUtil.showToastMessage(this@AiChat,"Error: ${e.message}")

                }
            }
        }
    }
    private fun showDeleteDialog(currentUserName: String, currentUid: String) {
        
        val dialogView = layoutInflater.inflate(R.layout.custom_dialog, null)
        val positiveButton = dialogView.findViewById<Button>(R.id.positiveButton)
        val negativeButton = dialogView.findViewById<Button>(R.id.negativeButton)

        val dialog = AlertDialog.Builder(this)
        dialog.setView(dialogView)
        dialog.setCancelable(true)
        val alertDialog = dialog.create()

        alertDialog.show()

        val dialogTitle = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val dialogSubTitle = dialogView.findViewById<TextView>(R.id.dialogSubTitle)
        dialogTitle.text = "Delete Message"
        dialogSubTitle.text = "Are you sure you want to delete this message?"


        positiveButton.setOnClickListener {
            deleteMessage(currentUserName, currentUid)
            alertDialog.dismiss()
        }
        negativeButton.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    private fun addMessageToChat(message: String, isUser: Boolean) {
        val sender = if (isUser) "user" else "bot"
        val timestamp = System.currentTimeMillis()
        val datestamp = System.currentTimeMillis()
        val messageObject = AiMessage(message, sender, timestamp, datestamp)
        messageList.add(messageObject)
        chatAdapter.notifyItemInserted(messageList.size - 1)
        binding.recyclerChat.scrollToPosition(messageList.size - 1)

        // Save message to Firebase
        val messageId = chatRef.push().key  // Generate a unique ID for each message
        messageId?.let {
            val messageData = mapOf(
                "message" to message,
                "sender" to sender,
                "timestamp" to timestamp,
                "datestamp" to datestamp
            )
            chatRef.child(it).setValue(messageData)
        }


    }

    private fun deleteMessage(currentUserName: String, currentUid: String) {
        val messageRef =
            FirebaseDatabase.getInstance().getReference("AiChats").child(currentUserName)
                .child(currentUid)

        messageRef.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                messageList.clear()
                chatAdapter.notifyDataSetChanged()
               // Toast.makeText(this, "Message deleted", Toast.LENGTH_SHORT).show()
                CommonUtil.showToastMessage(this,"Message deleted")

            } else {
               // Toast.makeText(this, "Failed to delete message", Toast.LENGTH_SHORT).show()
                CommonUtil.showToastMessage(this,"Failed to delete message")
            }
        }
    }

    private fun loadMessagesFromFirebase() {
        binding.progressBar.visibility = ProgressBar.VISIBLE
        chatRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messageList.clear() // Clear the list before loading new messages
                for (messageSnapshot in snapshot.children) {
                    val message = messageSnapshot.child("message").getValue(String::class.java)
                    val sender = messageSnapshot.child("sender").getValue(String::class.java)
                    val timestamp =
                        messageSnapshot.child("timestamp").getValue(Long::class.java) ?: 0L
                    val datestamp =
                        messageSnapshot.child("datestamp").getValue(Long::class.java) ?: 0L


                    if (message != null && sender != null) {
                        val messageObject = AiMessage(message, sender, timestamp, datestamp)
                        messageList.add(messageObject)
                    }
                }
                chatAdapter.notifyDataSetChanged()
                binding.recyclerChat.scrollToPosition(messageList.size - 1)
                binding.progressBar.visibility = ProgressBar.GONE


            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AiChat", "Failed to load messages: ${error.message}")
              //  Toast.makeText(this@AiChat, error.message, Toast.LENGTH_SHORT).show()
                CommonUtil.showToastMessage(this@AiChat,error.message)
                binding.progressBar.visibility = ProgressBar.GONE

            }
        })
    }


}

