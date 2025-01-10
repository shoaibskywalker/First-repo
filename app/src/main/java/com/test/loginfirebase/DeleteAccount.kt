package com.test.loginfirebase

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.test.loginfirebase.databinding.ActivityDeleteAccountBinding
import com.test.loginfirebase.utils.CommonUtil
import com.test.loginfirebase.utils.FirebaseUtil

class DeleteAccount : AppCompatActivity() {

    private lateinit var binding: ActivityDeleteAccountBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDeleteAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.deleteButton.text = "Send request to delete your account"
        binding.imageBack.setOnClickListener{
            finish()
        }
        window.statusBarColor =
            ContextCompat.getColor(this, R.color.normalColor)

        binding.radioGroupDeleteReason.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.radioOtherReason) {
                binding.etOtherReason.visibility = View.VISIBLE
            } else {
                binding.etOtherReason.visibility = View.GONE
            }
        }

        binding.deleteButton.setOnClickListener {
            val selectedReasonId = binding.radioGroupDeleteReason.checkedRadioButtonId
            if (selectedReasonId == -1) {
                CommonUtil.showToastMessage(this, "Please select a reason")
                return@setOnClickListener
            }

            val selectedReason = findViewById<RadioButton>(selectedReasonId).text.toString()
            val finalReason = if (selectedReason == "Other") {
                binding.emailEt.text.toString().takeIf { it.isNotBlank() }
                    ?: "No reason provided"
            } else {
                selectedReason
            }
            // Handle the reason (e.g., send to server or log it)
            showDeleteOptions(finalReason)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showDeleteOptions(finalReason: String) {

        val dialogView = layoutInflater.inflate(R.layout.delete_account_dialog, null)
        val whatsappButton = dialogView.findViewById<Button>(R.id.whatsappButton)
        val mailButton = dialogView.findViewById<Button>(R.id.mailButton)

        val dialog = AlertDialog.Builder(this)
        dialog.setView(dialogView)
        dialog.setCancelable(true)
        val alertDialog = dialog.create()

        alertDialog.show()

        val dialogTitle = dialogView.findViewById<TextView>(R.id.dialogTitle)
        dialogTitle.text = "How would you like to send the delete request?"



        whatsappButton.setOnClickListener {
            sendWhatsAppMessage(finalReason)
            alertDialog.dismiss()
        }
        mailButton.setOnClickListener {
            sendEmail(finalReason)
            alertDialog.dismiss()
        }
    }

    private fun sendEmail(finalReason: String) {
        val emailBody = Uri.encode("Hi, I would like to request the deletion of my account because $finalReason\n\nMy user Id is ${FirebaseUtil().currentUserId()}\n\nFrom LinkUp app")
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:shoaib.mansuri5242@gmail.com?subject=Account%20Deletion%20Request&body=$emailBody")
        }

        try {
            startActivity(Intent.createChooser(emailIntent, "Send email using:"))
        } catch (e: Exception) {
            CommonUtil.showToastMessage(this, "No email app found")
        }
    }

    private fun sendWhatsAppMessage(finalReason: String) {
        val phoneNumber = "+918424847976" // Replace with your support number
        val message =
            "Hi, I would like to request the deletion of my account because $finalReason\n\nMy user Id is ${FirebaseUtil().currentUserId()}\n\nFrom LinkUp app"

        val whatsappIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://wa.me/$phoneNumber?text=${Uri.encode(message)}")
        }

        try {
            startActivity(whatsappIntent)
        } catch (e: Exception) {
            CommonUtil.showToastMessage(this, "WhatsApp is not installed")

        }
    }

}
