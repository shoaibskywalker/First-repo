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

class SignUp : AppCompatActivity() {

    private lateinit var name : EditText
    private lateinit var email : EditText
    private lateinit var pass : EditText
    private lateinit var rePass : EditText
    private lateinit var buttonSign: Button
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressBar: ProgressBar
    private lateinit var databaseReference: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        name = findViewById(R.id.nameEtsign)
         email  = findViewById(R.id.emailEtsign)
         pass  = findViewById(R.id.passETsign)
         rePass  = findViewById(R.id.RePassETsign)
         buttonSign  = findViewById(R.id.buttonsign)
        progressBar = findViewById(R.id.progress)

firebaseAuth = FirebaseAuth.getInstance()
        progressBar.visibility=View.GONE

        buttonSign.setOnClickListener{
            val nameVar = name.text.toString()
            val emailVar = email.text.toString()
            val passVar = pass.text.toString()
            val rePassVar = rePass.text.toString()

            if (nameVar.isNotEmpty() && emailVar.isNotEmpty() && passVar.isNotEmpty() && rePassVar.isNotEmpty()){
                if (passVar == rePassVar){
                    firebaseAuth.createUserWithEmailAndPassword(emailVar,passVar).addOnCompleteListener {
                        if (it.isSuccessful){
                            addUserinDatabase(name,email,firebaseAuth.currentUser?.uid!!)
                            buttonSign.visibility=View.GONE
                            progressBar.visibility=View.VISIBLE
                            val intent = Intent(this,MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }else{
                            Toast.makeText(this,it.exception.toString(),Toast.LENGTH_SHORT).show()
                        }
                    }
                }else{
                    Toast.makeText(this,"Password is not matching",Toast.LENGTH_SHORT).show()
                }

            }else{
                Toast.makeText(this,"Your field is Empty",Toast.LENGTH_SHORT).show()
            }

        }

        setUpTextWatcher()

    }

    private fun addUserinDatabase(name: EditText, email: EditText, uid: String) {

        val nameVar = name.text.toString()
        val emailVar = email.text.toString()
        databaseReference = FirebaseDatabase.getInstance().getReference()
        databaseReference.child("User").child(uid).setValue(User(nameVar,emailVar,uid))

    }

    private fun setUpTextWatcher(){
        name.addTextChangedListener(object :TextWatcher{

            override fun afterTextChanged(p0: Editable?) {
                updateButton()
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }


        })


        email.addTextChangedListener(object :TextWatcher{

            override fun afterTextChanged(p0: Editable?) {
                updateButton()
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }


        })


        pass.addTextChangedListener(object :TextWatcher{

            override fun afterTextChanged(p0: Editable?) {
                updateButton()
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }



        })


        rePass.addTextChangedListener(object :TextWatcher{

            override fun afterTextChanged(p0: Editable?) {
                updateButton()
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }


        })
    }


    private fun updateButton(){
        val buttonSignn:Button  = findViewById(R.id.buttonsign)

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
