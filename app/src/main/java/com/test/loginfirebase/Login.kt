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
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.test.loginfirebase.databinding.ActivityLoginBinding
import com.test.loginfirebase.utils.sessionManager.UserSessionManager

class Login : AppCompatActivity() {


    private fun View.shake() {
        val shakeAnimator = ObjectAnimator.ofFloat(this, "translationX", -5f, 5f)
        shakeAnimator.duration = 90
        shakeAnimator.repeatCount = 4
        shakeAnimator.start()
    }

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var prefs: UserSessionManager
    private lateinit var binding: ActivityLoginBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))  // Get this from google-services.json
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        prefs = UserSessionManager(this)
        firebaseAuth = FirebaseAuth.getInstance()

        binding.googleSignInButton.setOnClickListener {
            signInWithGoogle()
        }

        val mailEt: EditText = findViewById(R.id.emailEt)
        val passEt: EditText = findViewById(R.id.passET)
        val buttonLog: Button = findViewById(R.id.buttonlog)
        val textLog: TextView = findViewById(R.id.textViewlog)
        val number: TextView = findViewById(R.id.number)

        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            // User is already logged in, navigate to the home screen
            navigateToHomeScreen()
            return // Finish this activity to prevent going back to the login screen
        }


        number.setOnClickListener {

            val intent = Intent(this, Phone::class.java)
            startActivity(intent)
        }


        buttonLog.setOnClickListener { view ->
            binding.progressBar.visibility = ProgressBar.VISIBLE
            binding.buttonlog.visibility = View.GONE
            val mailBind = mailEt.text.toString()
            val passBind = passEt.text.toString()

            if (isNetworkConnected()) {
                if (mailBind.isNotEmpty() && passBind.isNotEmpty()) {

                    firebaseAuth.signInWithEmailAndPassword(mailBind, passBind)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                binding.progressBar.visibility = ProgressBar.GONE
                                binding.buttonlog.visibility = View.VISIBLE
                                prefs.userEmailLogin = mailBind
                                val userNameLogin =
                                    mailBind.substringBefore('@').replace(Regex("[0-9]"), "")
                                        .replaceFirstChar { it.uppercase() }
                                prefs.userNameLogin = userNameLogin

                                showSnackbar(view, "Login Successfully")
                                val intent = Intent(this, MainActivity::class.java)
                                intent.flags =
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                                Toast.makeText(this, "Login Successfully", Toast.LENGTH_SHORT)
                                    .show()

                            } else {
                                showSnackbar(view, "Incorrect Password")
                                binding.progressBar.visibility = ProgressBar.GONE
                                binding.buttonlog.visibility = View.VISIBLE
                                vibrateTextField()
                                passEt.shake()
                                val passlayout: TextInputLayout = findViewById(R.id.passwordLayout)

                                passlayout.boxStrokeColor =
                                    ContextCompat.getColor(this, R.color.red)
                            }
                        }


                } else {
                    //Toast.makeText(this, "Your fields is empty", Toast.LENGTH_SHORT).show()
                    showSnackbar(view, "Your field is empty")
                }
            } else {
                showNoInternetDialog()
            }

        }

        textLog.setOnClickListener {
            if (isNetworkConnected()) {
                val intent = Intent(this, SignUp::class.java)
                startActivity(intent)
            } else {
                showNoInternetDialog()
            }
        }
        setupTextWatchers()


    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                // Handle failure
                Log.d("google signin error",e.message.toString())
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, navigate to your next activity
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Handle sign-in failure
                    Log.d("google signin error","daya kuch to gadbad hai")
                }
            }
    }

    private fun showSnackbar(view: View, message: String) {
        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
        val snackbarView = snackbar.view
        val snackbarText =
            snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        snackbarView.setBackgroundColor(ContextCompat.getColor(this, R.color.normalColor))
        snackbarText.setTextColor(ContextCompat.getColor(this, R.color.white))
        snackbar.show()
    }


    private fun navigateToHomeScreen() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish() // Finish this activity to prevent going back to the login screen
    }

    private fun isNetworkConnected(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
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
        val mailEt: EditText = findViewById(R.id.emailEt)
        val passEt: EditText = findViewById(R.id.passET)


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
        val buttonLog: Button = findViewById(R.id.buttonlog)

        val mailEt: EditText = findViewById(R.id.emailEt)
        val passEt: EditText = findViewById(R.id.passET)

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



