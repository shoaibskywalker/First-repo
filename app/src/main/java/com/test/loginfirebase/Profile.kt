package com.test.loginfirebase

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.test.loginfirebase.databinding.ActivityHelpBinding
import com.test.loginfirebase.databinding.ActivityProfileBinding
import com.test.loginfirebase.utils.sessionManager.UserSessionManager
import de.hdodenhof.circleimageview.CircleImageView

class Profile : AppCompatActivity() {

    private lateinit var imageBack: ImageView
    private lateinit var bgImage: ImageView
    private lateinit var profileImg: CircleImageView
    private lateinit var name: TextView
    private lateinit var email: TextView
    private lateinit var editImage: CircleImageView
    private lateinit var prefs: UserSessionManager

    private val PICK_IMAGE_REQUEST = 1
    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}