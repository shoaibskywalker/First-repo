package com.test.loginfirebase

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import androidx.biometric.BiometricManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.test.loginfirebase.databinding.ActivityProfile2Binding
import com.test.loginfirebase.utils.CommonUtil
import com.test.loginfirebase.utils.CommonUtil.showToastMessage
import com.test.loginfirebase.utils.FirebaseUtil
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
    private lateinit var appLockSwitch: Switch



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

        binding.appLockSwitch.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {
                // Check if the device supports biometrics
                val biometricManager = BiometricManager.from(this)
                if (biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS) {
                    // Show the biometrics option if available
                    binding.biometrics.visibility = View.VISIBLE
                    binding.biometrics.setOnClickListener {
                        showBiometricPrompt()
                    }
                } else {
                    // Show a message if biometrics is not supported
                    binding.biometrics.visibility = View.GONE
                    showToastMessage(
                        this,
                        "Biometric authentication is not supported on this device."
                    )
                }
            } else {
                // Hide the biometrics option when switch is turned off
                binding.biometrics.visibility = View.GONE
            }
        }


        binding.changePassword.setOnClickListener{
            val intent = Intent(this, ChangePasswordActivity::class.java)
            startActivity(intent)
        }

        window.statusBarColor = ContextCompat.getColor(this, R.color.normalColor) // Replace with your desired color

        binding.progressBar.visibility = ProgressBar.GONE

        userSessionManager = UserSessionManager(this)  // Initialize session manager
        // Get current user ID from FirebaseAuth
        val currentUserId = FirebaseUtil().currentUserId()
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
            showImageDialog(imageUrl)
        }

        binding.deleteImage.setOnClickListener {
            showDialog()
        }

    } private fun enableBiometricLock() {
        // Implement your biometric lock enabling logic here
        showToastMessage(this,"Biometric lock enabled!")
    }

    private fun showBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                // Handle successful authentication here
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                // Handle authentication error, reset the toggle if desired
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                // Handle failed authentication
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Angutha laga chutiye..")
            .setSubtitle("Authenticate using your biometric credential")
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    // Upload the image to Firebase Storage and store the URL in Firebase Realtime Database
    private fun uploadImageToFirebase(userId: String, bitmap: Bitmap) {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Updating your profile picture...") // Set your message
        progressDialog.setCancelable(false) // Prevents dismissal
        progressDialog.show()

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
                // Dismiss ProgressDialog when the task is complete
                progressDialog.dismiss()
            }
        }.addOnFailureListener {
            Log.e("Firebase Storage", "Image upload failed: ${it.message}")
            // Dismiss ProgressDialog when the task is complete
            progressDialog.dismiss()
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
        //-
        uid?.let {
            databaseReference.child(it).child("profileImageUrl").get()
                .addOnSuccessListener { snapshot ->
                     imageUrl = snapshot.value as? String
                    imageUrl?.let {
                       //---
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

    private fun showDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete!")
        builder.setMessage("Are you sure you want to remove your profile picture?")

        builder.setNegativeButton("No") { dialog, which ->
            // Handle the click event, e.g., close the app
            dialog.dismiss()
        }
        builder.setPositiveButton("Yes") { dialog, which ->
            val currentUserId = FirebaseUtil().currentUserId()
            currentUserId?.let { uid ->
                deleteProfileImageFromFirebase(uid)
            }
        }
        builder.setCancelable(true)
        builder.show()
    }

    private fun showImageDialog(imageUrl: String?) {
        val dialogBuilder = android.app.AlertDialog.Builder(this)
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_image_view, null)
        dialogBuilder.setView(dialogView)

        val imageView = dialogView.findViewById<ImageView>(R.id.dialogImageView)

        // Load the image into the ImageView
        imageUrl?.let {
            Glide.with(this)
                .load(it)
                .placeholder(R.drawable.portrait_placeholder)
                .error(R.drawable.portrait_placeholder)
                .into(imageView)
        }
        if (imageUrl.isNullOrEmpty()) {

            Glide.with(this)
                .load(R.drawable.portrait_placeholder)
                .placeholder(R.drawable.portrait_placeholder)
                .error(R.drawable.portrait_placeholder)
                .into(imageView)
        }

        val dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun deleteProfileImageFromFirebase(userId: String) {

        if (!imageUrl.isNullOrEmpty()) {
            binding.progressBar.visibility = ProgressBar.VISIBLE

            // Remove the image URL from Firebase Realtime Database
            databaseReference.child(userId).child("profileImageUrl").removeValue()
                .addOnSuccessListener {
                    Log.d("Firebase Realtime DB", "Image URL removed successfully")

                    // Now remove the image from Firebase Storage
                    val fileRef = storageReference.child("$userId.jpg")
                    fileRef.delete().addOnSuccessListener {
                        Log.d("Firebase Storage", "Image deleted successfully")

                        // Update UI after image is deleted
                        binding.image.setImageResource(R.drawable.portrait_placeholder)
                        imageUrl = null
                        binding.progressBar.visibility = ProgressBar.GONE
                    }.addOnFailureListener {
                        Log.e("Firebase Storage", "Failed to delete image: ${it.message}")
                        binding.progressBar.visibility = ProgressBar.GONE
                    }

                }.addOnFailureListener {
                    Log.e("Firebase Realtime DB", "Failed to remove image URL: ${it.message}")
                    binding.progressBar.visibility = ProgressBar.GONE
                }
        }else{
            //Toast.makeText(this, "No profile picture to delete", Toast.LENGTH_SHORT).show()
            CommonUtil.showToastMessage(this, "No profile picture to delete")

        }
    }

}