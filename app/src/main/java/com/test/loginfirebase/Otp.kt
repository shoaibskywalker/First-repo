package com.test.loginfirebase

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider

class Otp : AppCompatActivity() {

    private lateinit var verificationId: String
    private lateinit var progressBar: ProgressBar
    private lateinit var resend: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)

        resend = findViewById(R.id.resend)



        verificationId = intent.getStringExtra("verificationId").toString()

        val num1: EditText = findViewById(R.id.inputotp1)
        val num2: EditText = findViewById(R.id.inputotp2)
        val num3: EditText = findViewById(R.id.inputotp3)
        val num4: EditText = findViewById(R.id.inputotp4)
        val num5: EditText = findViewById(R.id.inputotp5)
        val num6: EditText = findViewById(R.id.inputotp6)
        val back: ImageView = findViewById(R.id.back)
        progressBar = findViewById(R.id.progressOtp)

        val buttonotp: Button = findViewById(R.id.buttonotp)

        buttonotp.setOnClickListener {

            progressBar.visibility = View.VISIBLE
            buttonotp.visibility = View.GONE

            val otp1 = num1.text.toString().trim()
            val otp2 = num2.text.toString().trim()
            val otp3 = num3.text.toString().trim()
            val otp4 = num4.text.toString().trim()
            val otp5 = num5.text.toString().trim()
            val otp6 = num6.text.toString().trim()

            if (otp1.isNotEmpty() && otp2.isNotEmpty() && otp3.isNotEmpty() && otp4.isNotEmpty() && otp5.isNotEmpty() && otp6.isNotEmpty()) {
                Toast.makeText(this, "OTP verified", Toast.LENGTH_SHORT).show()


                val code = otp1 + otp2 + otp3 + otp4 + otp5 + otp6
                val credential = PhoneAuthProvider.getCredential(verificationId, code)
                FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {

                    if (it.isSuccessful) {
                        progressBar.visibility = View.VISIBLE
                        buttonotp.visibility = View.GONE

                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    } else {
                        progressBar.visibility = View.GONE
                        buttonotp.visibility = View.VISIBLE
                        Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show()
                    }
                }


            } else {
                progressBar.visibility = View.GONE
                buttonotp.visibility = View.VISIBLE
                Toast.makeText(this, "Enter all number", Toast.LENGTH_SHORT).show()

            }
        }
        setupTextWatchers()

        back.setOnClickListener {
            finish()
        }
    }

    private fun setupTextWatchers() {
        val num1: EditText = findViewById(R.id.inputotp1)
        val num2: EditText = findViewById(R.id.inputotp2)
        val num3: EditText = findViewById(R.id.inputotp3)
        val num4: EditText = findViewById(R.id.inputotp4)
        val num5: EditText = findViewById(R.id.inputotp5)
        val num6: EditText = findViewById(R.id.inputotp6)


        num1.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }


            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }


            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed for this example
                if (s.toString().isNotEmpty()) {
                    num2.requestFocus()

                }
            }
        })

        num2.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }


            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }


            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed for this example
                if (s.toString().isNotEmpty()) {
                    num3.requestFocus()

                }
            }
        })

        num3.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }


            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }


            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed for this example
                if (s.toString().isNotEmpty()) {
                    num4.requestFocus()

                }
            }
        })

        num4.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }


            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }


            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed for this example
                if (s.toString().isNotEmpty()) {
                    num5.requestFocus()

                }
            }
        })

        num5.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }


            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }


            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed for this example
                if (s.toString().isNotEmpty()) {
                    num6.requestFocus()

                }
            }
        })

    }
}

