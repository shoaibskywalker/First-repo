package com.test.loginfirebase

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.test.loginfirebase.adapter.MessageAdapter
import com.test.loginfirebase.adapter.UserAdapter
import com.test.loginfirebase.data.Message
import de.hdodenhof.circleimageview.CircleImageView

class ChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var imageView: CircleImageView
    private lateinit var editText: EditText
    private lateinit var adapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>
    lateinit var databaseReference: DatabaseReference
    lateinit var imageBack:ImageView

    var senderRoom :String? = null
    var receiverRoom:String?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        imageBack = findViewById(R.id.imageBack)
        recyclerView = findViewById(R.id.recyclerChat)
        imageView=findViewById(R.id.sendMessageBtn)
        editText=findViewById(R.id.typeMessage)

        imageBack.setOnClickListener{
            finish()
        }

        databaseReference = FirebaseDatabase.getInstance().getReference()

        val receiverUid = intent.getStringExtra("uid")
        val senderUid = FirebaseAuth.getInstance().currentUser?.uid
        val name = intent.getStringExtra("name")
        val text=findViewById<TextView>(R.id.toolbar).apply {
            text=name
        }

        senderRoom = receiverUid + senderUid
        receiverRoom = senderUid + receiverUid


        messageList = ArrayList()
        adapter= MessageAdapter(this,messageList)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter=adapter

        databaseReference.child("chats")
            .child(senderRoom!!)
            .child("messages").addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {

                    messageList.clear()

                    for (postss in snapshot.children){

                        val message = postss.getValue(Message::class.java)
                        messageList.add(message!!)
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })


        imageView.setOnClickListener{

            val text = editText.text.toString().trim()
            val messageObject = Message(text,senderUid)

            databaseReference.child("chats")
                .child(senderRoom!!)
                .child("messages").push()
                .setValue(messageObject).addOnSuccessListener {

                    databaseReference.child("chats")
                        .child(receiverRoom!!)
                        .child("messages").push()
                        .setValue(messageObject)
                }
            editText.text.clear()

        }

    }
}
