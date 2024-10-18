package com.test.loginfirebase

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.test.loginfirebase.databinding.ActivityProfile2Binding
import com.test.loginfirebase.utils.sessionManager.UserSessionManager
import java.io.ByteArrayOutputStream

class Profile2 : AppCompatActivity() {

    private lateinit var binding: ActivityProfile2Binding
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    private lateinit var userSessionManager: UserSessionManager
    private var currentUserEmail: String? = null
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private var imageUrl: String? = null


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
        // Get current user ID from FirebaseAuth
        val currentUserId = firebaseAuth.currentUser?.uid
        databaseReference = FirebaseDatabase.getInstance().getReference("Users")

        // Initialize Firebase Storage reference
        storageReference = FirebaseStorage.getInstance().reference.child("userProfileImages")

        currentUserEmail = userSessionManager.userEmailLogin

        currentUserId?.let { uid ->
            loadProfileImageFromFirebase(uid)  // This loads the image when you re-enter the activity
        }

        // Load the saved image when the activity starts
        /*currentUserEmail?.let { email ->
            // Load the saved image for this user when the activity starts
            val savedImage = userSessionManager.getUserProfileImage(email)
            savedImage?.let {
                binding.image.setImageBitmap(it)
            }
        }*/

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
                    /*currentUserEmail?.let { email ->
                        userSessionManager.saveUserProfileImage(email, bitmap)
                    }*/
                    currentUserId?.let { uid ->
                        uploadImageToFirebase(uid, bitmap)
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
        binding.image.setOnClickListener {
            binding.fullimageview.visibility = View.VISIBLE
            Glide.with(this).load(imageUrl).into(binding.fullimageview)
        }

    }

    // Upload the image to Firebase Storage and store the URL in Firebase Realtime Database
    private fun uploadImageToFirebase(userId: String, bitmap: Bitmap) {
        // Convert bitmap to byte array
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageData = baos.toByteArray()

        val fileRef = storageReference.child("$userId.jpg")

        // Upload image to Firebase Storage
        val uploadTask = fileRef.putBytes(imageData)
        uploadTask.addOnSuccessListener {
            // Get the download URL of the uploaded image
            fileRef.downloadUrl.addOnSuccessListener { uri ->
                val imageUrl = uri.toString()
                // Save the image URL in Firebase Realtime Database
                saveImageUrlToDatabase(userId, imageUrl!!)
            }
        }.addOnFailureListener {
            Log.e("Firebase Storage", "Image upload failed: ${it.message}")
        }
    }

    // Save the image URL to Firebase Realtime Database
    private fun saveImageUrlToDatabase(userId: String, imageUrl: String) {
        databaseReference.child(userId).child("profileImageUrl").setValue(imageUrl)
            .addOnSuccessListener {
                Log.d("Firebase Realtime DB", "Image URL saved successfully $imageUrl")
                loadProfileImageFromFirebase(userId)
            }
            .addOnFailureListener {
                Log.e("Firebase Realtime DB", "Failed to save image URL: ${it.message}")
            }
    }

    private fun loadProfileImageFromFirebase(uid: String?) {
        binding.progressBar.visibility = ProgressBar.VISIBLE
        uid?.let {
            databaseReference.child(it).child("profileImageUrl").get()
                .addOnSuccessListener { snapshot ->
                     imageUrl = snapshot.value as? String
                    imageUrl?.let {
                        binding.progressBar.visibility = ProgressBar.GONE
                        loadProfileImage(it)
                    }
                }.addOnFailureListener {
                Log.e("Profile2 image upload", "Failed to load image URL from database: ${it.message}")
            }
        }
    }

    private fun loadProfileImage(imageUrl: String) {
        // Use any image loading library like Glide or Picasso
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.portrait_placeholder)
            .error(R.drawable.portrait_placeholder)
            .into(binding.image)
    }
}