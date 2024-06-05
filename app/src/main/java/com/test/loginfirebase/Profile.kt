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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        prefs = UserSessionManager(this)

        imageBack = findViewById(R.id.imageBack)
        bgImage = findViewById(R.id.backgroundImage)
        profileImg = findViewById(R.id.profileImage)
        name = findViewById(R.id.textName)
        email = findViewById(R.id.textEmail)
        editImage = findViewById(R.id.editImage)
        Log.d("sharedProfile", prefs.userEmailLogin.toString())
        val emaill = intent.getStringExtra("login")
email.text = emaill
        //Baack
        imageBack.setOnClickListener {
            finish()
        }

        //background Image
        bgImage.setOnLongClickListener {

            true
        }

        editImage.setOnClickListener {
            selectImage()
        }

    }

    protected fun selectImage() {
        val items = arrayOf<CharSequence>(
            "Take Photo", "Choose from Library",
            "Cancel"
        )
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add Photo!")
        builder.setItems(items) { dialog, item ->
            when {
                (items[item] == "Take Photo") -> {
                    showToast("Coming soon")
                }

                (items[item] == "Choose from Library") -> {
                    //showToast("Coming soon")
                    pickImageFromGallery()
                }

                (items[item] == "Cancel") -> dialog.dismiss()
            }
        }
        builder.show()

    }

    private fun pickImageFromGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri = data.data
            profileImg.setImageURI(imageUri)
            // Save the selected image URI in shared preferences
            prefs.userProfilePicture = imageUri.toString()
            // You can also store the image in your app's storage or upload it to a server
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}