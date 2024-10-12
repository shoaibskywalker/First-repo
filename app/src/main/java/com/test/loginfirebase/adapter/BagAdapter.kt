package com.test.loginfirebase.adapter

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.test.loginfirebase.R
import com.test.loginfirebase.data.BagList
import com.test.loginfirebase.databinding.BagListBinding

class BagAdapter(val context: Context, val bagList: List<BagList>) :
    RecyclerView.Adapter<BagAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = BagListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }


    override fun getItemCount(): Int {
        return bagList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = bagList[position]

        holder.binding.userName.text = currentItem.name
        holder.binding.userEmail.text = currentItem.email

        Glide.with(holder.itemView.context)
            .load(currentItem.image)
            .into(holder.binding.userImage)

        holder.binding.userImage.setOnClickListener {
            Log.d("image click", "Image clicked for ${currentItem.name}")
        }

    }

    class ViewHolder(val binding: BagListBinding) : RecyclerView.ViewHolder(binding.root)


}