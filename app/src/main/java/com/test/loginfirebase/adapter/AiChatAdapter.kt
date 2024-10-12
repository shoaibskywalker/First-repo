package com.test.loginfirebase.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.test.loginfirebase.R
import com.test.loginfirebase.data.AiMessage
import java.text.DateFormat
import java.util.Date

class AiChatAdapter(
    private val messageModalArrayList: ArrayList<AiMessage>,
    private val context: Context
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_USER = 0
        private const val VIEW_TYPE_BOT = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_USER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.sendmessage, parent, false)
                UserViewHolder(view)
            }
            VIEW_TYPE_BOT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.receivemessage, parent, false)
                BotViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val modal = messageModalArrayList[position]
        val timeFormatted = modal.timeStamp?.let { DateFormat.getTimeInstance().format(Date(it)) }

        when (holder) {
            is UserViewHolder -> {
                holder.userTV.text = modal.message
                holder.timeSend.text = timeFormatted
            }
            is BotViewHolder -> {
                holder.botTV.text = modal.message
                holder.timeReceive.text = timeFormatted
            }
        }
    }

    override fun getItemCount(): Int {
        return messageModalArrayList.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (messageModalArrayList[position].sender) {
            "user" -> 0
            "bot" -> 1
            else -> -1
        }
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userTV: TextView = itemView.findViewById(R.id.textSend)
        val timeSend = itemView.findViewById<TextView>(R.id.sendTimeSend)
    }

    inner class BotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val botTV: TextView = itemView.findViewById(R.id.textReceive)
        val timeReceive = itemView.findViewById<TextView>(R.id.sendTimeReceive)

    }
}
