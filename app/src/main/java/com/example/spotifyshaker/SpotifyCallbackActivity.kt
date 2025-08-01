package com.example.spotifyshaker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SpotifyCallbackActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Handle the callback from Spotify authentication
        val intent = intent
        val data = intent.data
        
        if (data != null) {
            // Pass the callback data back to MainActivity
            val resultIntent = Intent(this, MainActivity::class.java)
            resultIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            resultIntent.data = data
            startActivity(resultIntent)
        }
        
        // Close this activity
        finish()
    }
}