package com.test.loginfirebase

import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.ai.client.generativeai.GenerativeModel
import com.test.loginfirebase.adapter.AiChatAdapter
import com.test.loginfirebase.data.AiMessage
import com.test.loginfirebase.databinding.ActivityAiChatBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AiChat : AppCompatActivity() {

    private lateinit var binding: ActivityAiChatBinding

    private lateinit var chatAdapter: AiChatAdapter
    private val messageList = ArrayList<AiMessage>()
    var API_KEY = "AIzaSyDooL54T34k0lBf4vcmOTHQRDCAoSCN91s"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAiChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chatAdapter = AiChatAdapter(messageList, this)
        binding.recyclerChat.adapter = chatAdapter
        binding.recyclerChat.layoutManager = LinearLayoutManager(this)

        binding.onlineStatus.text = "Gemini 0.2.0"

        binding.progressBar.visibility = ProgressBar.GONE

        binding.imageBack.setOnClickListener {
            finish()
        }

        binding.sendMessageBtn.setOnClickListener {
            val userMessage = binding.typeMessage.text.toString().trim()

            if (userMessage.isNotEmpty()) {
                binding.progressBar.visibility = ProgressBar.VISIBLE
                addMessageToChat(userMessage, isUser = true)
                callGeminiApi(userMessage)

            } else {
                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
            }

            binding.typeMessage.text.clear()
        }
    }

    private fun callGeminiApi(prompt: String) {
        val generativeModel = GenerativeModel(
            modelName = "gemini-pro",
            apiKey = API_KEY
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = generativeModel.generateContent(prompt)
                withContext(Dispatchers.Main) {
                    addMessageToChat(response.text ?: "No response", isUser = false)
                    binding.progressBar.visibility = ProgressBar.GONE
                    Log.d("AI says :-", response.text.toString())
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AiChat, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = ProgressBar.GONE
                }
            }
        }
    }

    private fun addMessageToChat(message: String, isUser: Boolean) {
        val sender = if (isUser) "user" else "bot"
        val messageObject = AiMessage(message, sender, System.currentTimeMillis())
        messageList.add(messageObject)
        chatAdapter.notifyItemInserted(messageList.size - 1)
        binding.recyclerChat.scrollToPosition(messageList.size - 1)

    }

}

