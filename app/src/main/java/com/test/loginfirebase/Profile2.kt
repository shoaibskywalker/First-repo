package com.test.loginfirebase

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.test.loginfirebase.databinding.ActivityProfile2Binding
import com.test.loginfirebase.databinding.BagListBinding
import com.test.loginfirebase.utils.sessionManager.UserSessionManager

class Profile2 : AppCompatActivity() {

    private lateinit var binding: ActivityProfile2Binding
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    private lateinit var userSessionManager: UserSessionManager
    private var currentUserEmail: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProfile2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        userSessionManager = UserSessionManager(this)  // Initialize session manager

        currentUserEmail = userSessionManager.userEmailLogin

        // Load the saved image when the activity starts
        currentUserEmail?.let { email ->
            // Load the saved image for this user when the activity starts
            val savedImage = userSessionManager.getUserProfileImage(email)
            savedImage?.let {
                binding.image.setImageBitmap(it)
            }
        }

        binding.imageBack.setOnClickListener {
            finish()
        }

        pickImageLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val imageUri: Uri? = result.data!!.data
                imageUri?.let { uri ->
                    // Using MediaStore to access the image
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                    binding.image.setImageBitmap(bitmap)  // Display in ImageView
                    Log.d("selected image", uri.toString())
                    // Save the selected image to SharedPreferences
                    // Save the selected image for the current user
                    currentUserEmail?.let { email ->
                        userSessionManager.saveUserProfileImage(email, bitmap)
                    }
                }
            }
        }

        binding.imageEdit.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            pickImageLauncher.launch(intent)
        }
        binding.emailProfile.text = userSessionManager.userEmailLogin
    }
}