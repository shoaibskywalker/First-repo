package com.test.loginfirebase

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.test.loginfirebase.data.User
import com.test.loginfirebase.databinding.ActivityMainBinding
import com.test.loginfirebase.databinding.ActivitySignUpBinding
import com.test.loginfirebase.utils.CommonUtil
import com.test.loginfirebase.utils.FirebaseUtil
import com.test.loginfirebase.utils.sessionManager.UserSessionManager

class SignUp : AppCompatActivity() {

    private lateinit var name: EditText
    private lateinit var email: EditText
    private lateinit var pass: EditText
    private lateinit var rePass: EditText
    private lateinit var buttonSign: Button
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressBar: ProgressBar
    private lateinit var databaseReference: DatabaseReference
    private lateinit var prefs: UserSessionManager
    private lateinit var binding: ActivitySignUpBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = UserSessionManager(this)

        name = findViewById(R.id.nameEtsign)
        email = findViewById(R.id.emailEtsign)
        pass = findViewById(R.id.passETsign)
        rePass = findViewById(R.id.RePassETsign)
        buttonSign = findViewById(R.id.buttonsign)
        progressBar = findViewById(R.id.progress)

        firebaseAuth = FirebaseAuth.getInstance()


        buttonSign.setOnClickListener {
            binding.progress.visibility = ProgressBar.VISIBLE
            binding.buttonsign.visibility = View.GONE
            val nameVar = name.text.toString()
            val emailVar = email.text.toString()
            val passVar = pass.text.toString()
            val rePassVar = rePass.text.toString()

            prefs.userNameSignup = nameVar
            prefs.userEmailSignup = emailVar

            if (nameVar.isNotEmpty() && emailVar.isNotEmpty() && passVar.isNotEmpty() && rePassVar.isNotEmpty()) {
                if (passVar == rePassVar) {
                    firebaseAuth.createUserWithEmailAndPassword(emailVar, passVar)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                binding.progress.visibility = ProgressBar.GONE
                                binding.buttonsign.visibility = View.VISIBLE
                                val currentUid = FirebaseUtil().currentUserId()!!
                                addUserinDatabase(name, email,currentUid,pass)
                                val intent = Intent(this, MainActivity::class.java)
                                intent.flags =
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                intent.putExtra("name", nameVar)
                                intent.putExtra("email", emailVar)
                                intent.putExtra("source", "signup")
                                startActivity(intent)
                            } else {
                              /*  Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT)
                                    .show()*/
                                CommonUtil.showToastMessage(this, it.exception.toString())

                                binding.progress.visibility = ProgressBar.GONE
                                binding.buttonsign.visibility = View.VISIBLE
                            }
                        }
                } else {
                  //  Toast.makeText(this, "Password is not matching", Toast.LENGTH_SHORT).show()
                    CommonUtil.showToastMessage(this, "Password is not matching")

                    binding.progress.visibility = ProgressBar.GONE
                    binding.buttonsign.visibility = View.VISIBLE
                }

            } else {
               // Toast.makeText(this, "Your field is Empty", Toast.LENGTH_SHORT).show()
                CommonUtil.showToastMessage(this, "Your field is Empty")

            }

        }

        setUpTextWatcher()

    }

    private fun addUserinDatabase(name: EditText, email: EditText, uid: String,password: EditText) {

        val nameVar = name.text.toString()
        val emailVar = email.text.toString()
        val passVar = password.text.toString()
        databaseReference = FirebaseDatabase.getInstance().getReference()
        databaseReference.child("User").child(nameVar).setValue(User(name = nameVar, email =  emailVar, uid =  uid, pass = passVar))

    }

    private fun setUpTextWatcher() {
        name.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(p0: Editable?) {
                updateButton()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }


        })


        email.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(p0: Editable?) {
                updateButton()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }


        })


        pass.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(p0: Editable?) {
                updateButton()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }


        })


        rePass.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(p0: Editable?) {
                updateButton()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }


        })
    }


    private fun updateButton() {
        val buttonSignn: Button = findViewById(R.id.buttonsign)

        val n = name.text.toString()
        val e = email.text.toString()
        val p = pass.text.toString()
        val rp = rePass.text.toString()

        val isNotEmpty = n.isNotBlank() && e.isNotBlank() && p.isNotBlank() && rp.isNotBlank()
        buttonSignn.isEnabled = isNotEmpty

        if (isNotEmpty) {
            buttonSign.setBackgroundColor(getColor(R.color.normalColor))
            buttonSign.setTextColor(getColor(R.color.white))

        } else {
            buttonSign.setBackgroundColor(getColor(R.color.disabledColor)) // Set grey color
            buttonSign.setTextColor(getColor(R.color.disabledColor))
        }
    }


}
