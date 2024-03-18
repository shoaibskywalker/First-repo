package com.test.loginfirebase

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {

    private fun View.shake() {
        val shakeAnimator = ObjectAnimator.ofFloat(this, "translationX", -5f, 5f)
        shakeAnimator.duration = 90
        shakeAnimator.repeatCount = 4
        shakeAnimator.start()
    }
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        firebaseAuth = FirebaseAuth.getInstance()

        val mailEt:EditText = findViewById(R.id.emailEt)
        val passEt:EditText = findViewById(R.id.passET)
        val buttonLog:Button = findViewById(R.id.buttonlog)
        val textLog:TextView = findViewById(R.id.textViewlog)
        val number : TextView = findViewById(R.id.number)

        number.setOnClickListener {

            val intent = Intent(this,Phone::class.java)
            startActivity(intent)
        }


        buttonLog.setOnClickListener {

            val mailBind = mailEt.text.toString()
            val passBind = passEt.text.toString()

if (isNetworkConnected()) {
    if (mailBind.isNotEmpty() && passBind.isNotEmpty()) {

        firebaseAuth.signInWithEmailAndPassword(mailBind, passBind).addOnCompleteListener {
            if (it.isSuccessful) {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            } else {

                Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show()

                vibrateTextField()
                passEt.shake()
                val passlayout: TextInputLayout = findViewById(R.id.passwordLayout)

                passlayout.boxStrokeColor = ContextCompat.getColor(this, R.color.red)


            }
        }

    } else {
        Toast.makeText(this, "Your fields is empty", Toast.LENGTH_SHORT).show()
    }
}else{
    showNoInternetDialog()
}


            }

            textLog.setOnClickListener {
if (isNetworkConnected()) {
    val intent = Intent(this, SignUp::class.java)
    startActivity(intent)
}else{
    showNoInternetDialog()
}
            }
        setupTextWatchers()




    }


    private fun isNetworkConnected(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    private fun showNoInternetDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("No Internet Connection")
        builder.setMessage("Please check your internet connection and try again.")

        builder.setPositiveButton("OK") { dialog, which ->
            // Handle the click event, e.g., close the app
            finish()
        }
        builder.setCancelable(false)
        builder.show()


    }



    private fun setupTextWatchers() {
        val mailEt:EditText = findViewById(R.id.emailEt)
        val passEt:EditText = findViewById(R.id.passET)


        mailEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateLoginButtonState()
            }


            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }


            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed for this example
            }
        })

        passEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateLoginButtonState()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed for this example
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed for this example
            }
        })

        // Initial state
        updateLoginButtonState()
    }

    private fun updateLoginButtonState() {
        val buttonLog:Button = findViewById(R.id.buttonlog)

        val mailEt:EditText = findViewById(R.id.emailEt)
        val passEt:EditText = findViewById(R.id.passET)

        val email = mailEt.text.toString()
        val password = passEt.text.toString()

        val isNotEmpty = email.isNotBlank() && password.isNotBlank()
        buttonLog.isEnabled = isNotEmpty

        if (isNotEmpty) {
            buttonLog.setBackgroundColor(getColor(R.color.normalColor))
            buttonLog.setTextColor(getColor(R.color.white))

        } else {
            buttonLog.setBackgroundColor(getColor(R.color.disabledColor)) // Set grey color
            buttonLog.setTextColor(getColor(R.color.disabledColor))
        }

    }

    private fun vibrateTextField() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?

        // Check if the device has a vibrator
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        150,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(150)
            }
        }
    }
}



