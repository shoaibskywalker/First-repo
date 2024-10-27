package com.test.loginfirebase

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.test.loginfirebase.databinding.ActivityForgotPasswordBinding
import com.test.loginfirebase.utils.CommonUtil

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imageBack.setOnClickListener {
            finish()
        }

        firebaseAuth = FirebaseAuth.getInstance()

        binding.buttonforgetpass.setOnClickListener {
            val email = binding.emailEt.text.toString().trim()
            if (email.isNotEmpty()) {
                binding.progressBar.visibility = ProgressBar.VISIBLE
                binding.buttonforgetpass.visibility = View.GONE
                sendResetPasswordEmail(email)
            } else {
               // Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                CommonUtil.showToastMessage(this,"Please enter your email")

            }
        }
    }

    private fun sendResetPasswordEmail(email: String) {
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    binding.progressBar.visibility = ProgressBar.GONE
                    binding.buttonforgetpass.visibility = View.VISIBLE
                  //  Toast.makeText(this, "Reset link sent to your email", Toast.LENGTH_SHORT).show()
                    CommonUtil.showToastMessage(this,"Reset link sent to your email")
                } else {
                    binding.progressBar.visibility = ProgressBar.GONE
                    binding.buttonforgetpass.visibility = View.VISIBLE
                   // Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    CommonUtil.showToastMessage(this,"Error: ${task.exception?.message}")
                }
            }
    }
}