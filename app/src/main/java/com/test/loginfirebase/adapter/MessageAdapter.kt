package com.test.loginfirebase.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.firebase.auth.FirebaseAuth
import com.test.loginfirebase.R
import com.test.loginfirebase.data.Message

class MessageAdapter(val context: Context, val messageList: ArrayList<Message>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val ITEM_RECEIVE = 1
    val ITEM_SEND = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        if (viewType==1){
            val view = LayoutInflater.from(context).inflate(R.layout.receivemessage,parent,false)
            return ReceiveViewHolder(view)
        }else{
            val view = LayoutInflater.from(context).inflate(R.layout.sendmessage,parent,false)
            return SendViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun getItemViewType(position: Int): Int {
        val currentpossition = messageList[position]

        return if (FirebaseAuth.getInstance().currentUser?.uid.equals(currentpossition.senderId)) {
            ITEM_SEND
        }else{
            ITEM_RECEIVE
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentpossition = messageList[position]

        if (holder.javaClass == SendViewHolder::class.java) {

            val viewHolder = holder as SendViewHolder

            holder.sendText.text = currentpossition.message
        } else {
            val viewHolder = holder as ReceiveViewHolder

            holder.receiveText.text = currentpossition.message
        }
    }


    class SendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val sendText = itemView.findViewById<TextView>(R.id.textSend)

    }

    class ReceiveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val receiveText = itemView.findViewById<TextView>(R.id.textReceive)

    }

}
