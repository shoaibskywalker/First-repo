package com.test.loginfirebase

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.test.loginfirebase.adapter.UserAdapter
import com.test.loginfirebase.data.User
import com.test.loginfirebase.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

   private lateinit var recyclerView: RecyclerView
    lateinit var mAdapter: UserAdapter
    lateinit var userList: ArrayList<User>
    private lateinit var binding: ActivityMainBinding
    private lateinit var mAuth:FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerView = findViewById(R.id.recyclerView)

        mAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference()

        userList = ArrayList()
        mAdapter= UserAdapter(this,userList)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = mAdapter

        databaseReference.child("User").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                userList.clear()
                for (postSnapshot in snapshot.children) {

                    val currentUser = postSnapshot.getValue(User::class.java)
                    if (mAuth.currentUser?.uid!=currentUser?.uid) {

                        userList.add(currentUser!!)
                    }

                }
                mAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }
}
