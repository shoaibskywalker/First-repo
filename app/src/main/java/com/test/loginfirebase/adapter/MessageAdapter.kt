package com.test.loginfirebase.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.test.loginfirebase.R
import com.test.loginfirebase.data.Message
import com.test.loginfirebase.utils.FirebaseUtil
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessageAdapter(val context: Context, val messageList: ArrayList<Message>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val ITEM_RECEIVE = 1
    val ITEM_SEND = 2

    var onMessageLongClickListener: ((Message) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        if (viewType == 1) {
            val view = LayoutInflater.from(context).inflate(R.layout.receivemessage, parent, false)
            return ReceiveViewHolder(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.sendmessage, parent, false)
            return SendViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun getItemViewType(position: Int): Int {
        val currentpossition = messageList[position]

        return if (FirebaseUtil().currentUserId().equals(currentpossition.senderId)) {
            ITEM_SEND
        } else {
            ITEM_RECEIVE
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentpossition = messageList[position]

        if (holder.javaClass == SendViewHolder::class.java) {

            val viewHolder = holder as SendViewHolder

            holder.sendText.text = currentpossition.message
            holder.timeSend.text = currentpossition.timeStamp?.let {
                Date(
                    it
                )
            }?.let { SimpleDateFormat("hh:mm a", Locale.getDefault()).format(it) }

            holder.dateSend.text = currentpossition.dateStamp?.let {
                Date(
                    it
                )
            }?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it) }

            viewHolder.itemView.setOnLongClickListener {
                onMessageLongClickListener!!.invoke(currentpossition)
                true
            }
        } else {
            val viewHolderReceive = holder as ReceiveViewHolder

            holder.receiveText.text = currentpossition.message
            holder.timeReceive.text = currentpossition.timeStamp?.let {
                Date(
                    it
                )
            }?.let { SimpleDateFormat("hh:mm a", Locale.getDefault()).format(it) }

            holder.dateReceive.text = currentpossition.dateStamp?.let {
                Date(
                    it
                )
            }?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it) }

            viewHolderReceive.itemView.setOnLongClickListener {
                onMessageLongClickListener!!.invoke(currentpossition)
                true
            }
        }
    }


    class SendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val sendText = itemView.findViewById<TextView>(R.id.textSend)
        val timeSend = itemView.findViewById<TextView>(R.id.sendTimeSend)
        val dateSend = itemView.findViewById<TextView>(R.id.dateSend)


    }

    class ReceiveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val receiveText = itemView.findViewById<TextView>(R.id.textReceive)
        val timeReceive = itemView.findViewById<TextView>(R.id.sendTimeReceive)
        val dateReceive = itemView.findViewById<TextView>(R.id.dateReceive)


    }

}
