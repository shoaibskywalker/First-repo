package com.test.loginfirebase

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Help : AppCompatActivity() {

    private lateinit var imageBack : ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        imageBack = findViewById(R.id.imageBack)
        imageBack.setOnClickListener{
            finish()
        }

    }
}