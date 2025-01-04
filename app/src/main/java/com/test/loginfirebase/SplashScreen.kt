package com.test.loginfirebase

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.test.loginfirebase.databinding.SplashBinding

class SplashScreen : AppCompatActivity() {

    private lateinit var binding: SplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*Handler().postDelayed({
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }, 3000)*/

        startLetterDisplayAnimation()
    }

    private fun startLetterDisplayAnimation() {
        val delays = listOf(
            1000L to "L",
            1100L to "i",
            1200L to "n",
            1300L to "k",
            1400L to "U",
            1500L to "p"
        )

        delays.forEach { (delay, text) ->
            Handler().postDelayed({
                binding.linkup.append(text) // Append each letter, so the previous letters stay visible
            }, delay)
        }

        // Calculate the total animation duration
        val totalAnimationDuration = delays.last().first + 2000L // Add 2 seconds after animation

        Handler().postDelayed({
            navigateToMainActivity()
        }, totalAnimationDuration)
    }


    private fun navigateToMainActivity() {
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
        finish()
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
    }
}