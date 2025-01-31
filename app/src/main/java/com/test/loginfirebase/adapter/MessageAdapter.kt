package com.test.loginfirebase.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.test.loginfirebase.R
import com.test.loginfirebase.data.Message
import com.test.loginfirebase.utils.FirebaseUtil
import com.test.loginfirebase.utils.sessionManager.UserSessionManager
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessageAdapter(val context: Context, val messageList: ArrayList<Message>) :
    RecyclerView.Adapter<ViewHolder>() {

    private val prefs: UserSessionManager by lazy {
        UserSessionManager(context)
    }

    val ITEM_RECEIVE = 1
    val ITEM_SEND = 2
    val ITEM_TYPING = 3

    var onMessageLongClickListener: ((Message) -> Unit)? = null
    var onMessageClickListener: ((Message) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return when (viewType) {
            ITEM_TYPING -> {
                val view =
                    LayoutInflater.from(context).inflate(R.layout.typing_indicator, parent, false)
                TypingViewHolder(view)
            }

            ITEM_RECEIVE -> {
                val view =
                    LayoutInflater.from(context).inflate(R.layout.receivemessage, parent, false)
                ReceiveViewHolder(view)
            }

            else -> {
                val view = LayoutInflater.from(context).inflate(R.layout.sendmessage, parent, false)
                SendViewHolder(view)
            }
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun getItemViewType(position: Int): Int {
        val currentpossition = messageList[position]

        return when {
            currentpossition.isTyping -> ITEM_TYPING // New type for typing indicator
            FirebaseUtil().currentUserId() == currentpossition.senderId -> ITEM_SEND
            else -> ITEM_RECEIVE
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentpossition = messageList[position]

        when (holder) {
            is SendViewHolder -> {
                holder.sendText.text = currentpossition.message
                holder.timeSend.text = currentpossition.timeStamp?.let {
                    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(it))
                }
                holder.dateSend.text = currentpossition.dateStamp?.let {
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it))
                }
                holder.itemView.setOnLongClickListener {
                    onMessageLongClickListener?.invoke(currentpossition)
                    true
                }
            }

            is ReceiveViewHolder -> {
                holder.receiveText.text = currentpossition.message
                holder.timeReceive.text = currentpossition.timeStamp?.let {
                    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(it))
                }
                holder.dateReceive.text = currentpossition.dateStamp?.let {
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it))
                }

                holder.itemView.setOnLongClickListener {
                    onMessageLongClickListener?.invoke(currentpossition)
                    true
                }

                /*prefs.receiverUserPicture.let {
                    Glide.with(context)
                        .load(it)
                        .placeholder(R.drawable.portrait_placeholder)
                        .error(R.drawable.portrait_placeholder)
                        // Ensure it's not loading from cache
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.imageChat)
                }*/

                // Determine if the image should be visible
                val isLastMessageFromSender = position == messageList.size - 1 ||
                        messageList[position + 1].senderId != currentpossition.senderId

                if (isLastMessageFromSender) {
                    holder.imageChat.visibility = View.VISIBLE
                    prefs.receiverUserPicture.let {
                        Glide.with(context)
                            .load(it)
                            .placeholder(R.drawable.portrait_placeholder)
                            .error(R.drawable.portrait_placeholder)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(holder.imageChat)
                    }
                } else {
                    holder.imageChat.visibility = View.INVISIBLE
                }

                holder.imageChat.setOnClickListener {
                    onMessageClickListener?.invoke(currentpossition)
                    true
                }
            }

            is TypingViewHolder -> {
                // TypingViewHolder requires no binding since it's static (just an ImageView)
                holder.typingIndicatior.visibility = View.VISIBLE
            }
        }
    }


    class SendViewHolder(itemView: View) : ViewHolder(itemView) {

        val sendText = itemView.findViewById<TextView>(R.id.textSend)!!
        val timeSend = itemView.findViewById<TextView>(R.id.sendTimeSend)!!
        val dateSend = itemView.findViewById<TextView>(R.id.dateSend)!!
        val feeling = itemView.findViewById<CircleImageView>(R.id.feeling)

    }

    class ReceiveViewHolder(itemView: View) : ViewHolder(itemView) {

        val receiveText = itemView.findViewById<TextView>(R.id.textReceive)!!
        val timeReceive = itemView.findViewById<TextView>(R.id.sendTimeReceive)!!
        val dateReceive = itemView.findViewById<TextView>(R.id.dateReceive)!!
        val imageChat = itemView.findViewById<CircleImageView>(R.id.imageChat)
        val feeling = itemView.findViewById<CircleImageView>(R.id.feeling)

    }

    class TypingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val typingIndicatior = itemView.findViewById<ConstraintLayout>(R.id.indicator)
    }


}
