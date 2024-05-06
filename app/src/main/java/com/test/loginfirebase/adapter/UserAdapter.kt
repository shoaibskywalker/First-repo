package com.test.loginfirebase.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.test.loginfirebase.ChatActivity
import com.test.loginfirebase.R

import com.test.loginfirebase.data.User

class UserAdapter(val context: Context, val  name : ArrayList<User>, private var filteredList: ArrayList<User>):RecyclerView.Adapter<UserAdapter.UserViewholder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewholder {

        val view = LayoutInflater.from(context).inflate(R.layout.user_layout,parent,false)
        return UserViewholder(view)

    }

    override fun getItemCount(): Int {

        return filteredList.size
    }

    override fun onBindViewHolder(holder: UserViewholder, position: Int) {

        val currentUser = filteredList[position]
        holder.text.text=currentUser.name

        holder.itemView.setOnClickListener {

            val intent = Intent(context,ChatActivity::class.java)

            intent.putExtra("name",currentUser.name)
            intent.putExtra("uid",currentUser.uid)

            context.startActivity(intent)
        }



    }

    class UserViewholder(itemview: View):RecyclerView.ViewHolder(itemview) {

        val text = itemview.findViewById<TextView>(R.id.txtName)
    }
    fun filterList(filteredList: ArrayList<User>) {
        this.filteredList = filteredList
        notifyDataSetChanged()
    }

}


