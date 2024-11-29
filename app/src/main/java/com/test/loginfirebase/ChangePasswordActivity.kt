package com.test.loginfirebase

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.test.loginfirebase.databinding.ActivityChangePasswordBinding
import com.test.loginfirebase.utils.CommonUtil
import com.test.loginfirebase.utils.sessionManager.UserSessionManager

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangePasswordBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var prefs: UserSessionManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        prefs = UserSessionManager(this)

        binding.imageBack.setOnClickListener {
            finish()
        }

        databaseReference = FirebaseDatabase.getInstance().getReference()

        binding.buttonChangePassword.setOnClickListener {
            val currentPassword = binding.currentPassword.text.toString().trim()
            val newPassword = binding.newpassword.text.toString().trim()
            val confirmPassword = binding.confirmPassword.text.toString().trim()

            if (newPassword == confirmPassword) {
                binding.progressBar.visibility = ProgressBar.VISIBLE
                binding.buttonChangePassword.visibility = View.GONE
                if (currentPassword.isNotEmpty() && newPassword.isNotEmpty() && confirmPassword.isNotEmpty()) {
                    binding.progressBar.visibility = ProgressBar.VISIBLE
                    binding.buttonChangePassword.visibility = View.GONE
                    reauthenticateAndChangePassword(currentPassword, newPassword)
                } else {
                    Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = ProgressBar.GONE
                    binding.buttonChangePassword.visibility = View.VISIBLE
                }
            } else {
                Toast.makeText(this, "Password does not match", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = ProgressBar.GONE
                binding.buttonChangePassword.visibility = View.VISIBLE
            }
        }
    }

    private fun reauthenticateAndChangePassword(currentPassword: String, newPassword: String) {
        if (currentPassword != newPassword) {
            val user = firebaseAuth.currentUser
            user?.let {
                val credential = EmailAuthProvider.getCredential(it.email!!, currentPassword)
                it.reauthenticate(credential).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        user.updatePassword(newPassword).addOnCompleteListener { updateTask ->
                            binding.progressBar.visibility = ProgressBar.GONE
                            binding.buttonChangePassword.visibility = View.VISIBLE
                            if (updateTask.isSuccessful) {
                                binding.currentPassword.text?.clear()
                                binding.newpassword.text?.clear()
                                binding.confirmPassword.text?.clear()
                                val currentUserId = firebaseAuth.currentUser?.uid
                                Log.d("Current user ID", currentUserId.toString())
                                databaseReference.child("ChangePassword").child(currentUserId!!)
                                    .child(prefs.userNameLogin!!).child("ChangedPassword")
                                    .setValue(newPassword)

                               /* Toast.makeText(
                                    this,
                                    "Password updated successfully",
                                    Toast.LENGTH_SHORT
                                ).show()*/
                                CommonUtil.showToastMessage(this,"Password updated successfully")
                            } else {
                                binding.currentPassword.text?.clear()
                                binding.newpassword.text?.clear()
                                binding.confirmPassword.text?.clear()
                               /* Toast.makeText(this, "Password update failed", Toast.LENGTH_SHORT)
                                    .show()*/
                                CommonUtil.showToastMessage(this,"Password update failed")

                            }
                        }
                    } else {
                        binding.progressBar.visibility = ProgressBar.GONE
                        binding.buttonChangePassword.visibility = View.VISIBLE
                       /* Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show();
                        Toast.makeText(this, "Re-authentication failed", Toast.LENGTH_SHORT).show()*/
                        CommonUtil.showToastMessage(this,"Incorrect password")
                        CommonUtil.showToastMessage(this,"Re-authentication failed")

                    }
                }
            }
        } else {
            binding.currentPassword.text?.clear()
          //  Toast.makeText(this, "Your password is same", Toast.LENGTH_SHORT).show()
            CommonUtil.showToastMessage(this,"Your password is same")

        }
    }
}