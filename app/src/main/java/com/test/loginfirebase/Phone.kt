package com.test.loginfirebase

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class Phone : AppCompatActivity() {

    private lateinit var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var buttonOtp: Button
    private lateinit var textNumber: EditText
    private lateinit var backNum: ImageView
    private lateinit var progressBar: ProgressBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone)

        firebaseAuth = FirebaseAuth.getInstance()

        buttonOtp = findViewById(R.id.buttongetotp)
        textNumber = findViewById(R.id.phoneEtotp)
        backNum = findViewById(R.id.backnum)
        progressBar = findViewById(R.id.progressPhone)

        backNum.setOnClickListener {
            finish()
        }


        buttonOtp.setOnClickListener {
            val text = textNumber.text.trim().toString()
            if (text.isNotEmpty()) {
                otpSend()
            } else {
                Toast.makeText(this, "Please enter Number", Toast.LENGTH_SHORT).show()

            }
        }
    }

    private fun otpSend() {

        progressBar.visibility = View.VISIBLE
        buttonOtp.visibility = View.GONE

        mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {


            override fun onVerificationCompleted(credential: PhoneAuthCredential) {

            }

            override fun onVerificationFailed(e: FirebaseException) {
                progressBar.visibility = View.GONE
                buttonOtp.visibility = View.VISIBLE
                Toast.makeText(this@Phone, e.localizedMessage, Toast.LENGTH_LONG).show()

            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {

                val text = textNumber.text.trim().toString()


                progressBar.visibility = View.GONE
                buttonOtp.visibility = View.VISIBLE

                if (text.length == 10) {
                    val intent = Intent(this@Phone, Otp::class.java)
                    intent.putExtra("verificationId", verificationId)
                    startActivity(intent)

                } else {
                    Toast.makeText(this@Phone, "Please enter correct Number", Toast.LENGTH_SHORT)
                        .show()
                }

            }
        }

        val text = textNumber.text.trim().toString()
        val formattedPhoneNumber =
            PhoneNumberUtils.formatNumberToE164(text, "IN") // "IN" is the country code for India
        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(formattedPhoneNumber) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // Activity (for callback binding)
            .setCallbacks(mCallbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)

    }
}



