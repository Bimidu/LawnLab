package com.example.madlabexam03

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class WinActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_win)

        // Find the button by its ID
        val btnBackToMain = findViewById<Button>(R.id.backtomainfromwin)

        // Set click listener for the button
        btnBackToMain.setOnClickListener {
            // Create an intent to navigate back to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

            // Finish this activity (optional, depending on whether you want to keep the WinActivity in the back stack)
            finish()
        }
    }
}