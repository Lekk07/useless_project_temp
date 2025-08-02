package com.example.spotifyshaker

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.spotifyshaker.databinding.ActivityMainBinding
import com.spotify.protocol.types.Track
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), 
    SpotifyManager.SpotifyConnectionListener,
    ShakeDetector.OnShakeListener {
    
    companion object {
        private const val TAG = "MainActivity"
    }
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var spotifyManager: SpotifyManager
    private lateinit var shakeDetector: ShakeDetector
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        initializeComponents()
        setupUI()
    }
    
    private fun initializeComponents() {
        // Initialize Spotify Manager
        spotifyManager = SpotifyManager(this)
        spotifyManager.setConnectionListener(this)
        
        // Initialize Shake Detector
        shakeDetector = ShakeDetector(this)
        shakeDetector.setOnShakeListener(this)
        
        // Check if accelerometer is available
        if (!shakeDetector.isAccelerometerAvailable()) {
            showToast("Accelerometer not available on this device")
            binding.shakeStatusText.text = "Accelerometer not available"
        }
    }
    
    private fun setupUI() {
        binding.connectButton.setOnClickListener {
            if (spotifyManager.isConnected()) {
                spotifyManager.disconnect()
            } else {
                spotifyManager.connect()
            }
        }
        
        binding.startShakeDetectionButton.setOnClickListener {
            if (shakeDetector.isListening()) {
                stopShakeDetection()
            } else {
                startShakeDetection()
            }
        }
        
        binding.testShakeButton.setOnClickListener {
            onShake() // Manually trigger shake for testing
        }
        
        updateUI()
    }
    
    private fun startShakeDetection() {
        if (spotifyManager.isConnected()) {
            shakeDetector.startListening()
            updateUI()
            showToast("Shake detection started")
            Log.d(TAG, "Shake detection started")
        } else {
            showToast("Please connect to Spotify first")
        }
    }
    
    private fun stopShakeDetection() {
        shakeDetector.stopListening()
        updateUI()
        showToast("Shake detection stopped")
        Log.d(TAG, "Shake detection stopped")
    }
    
    private fun updateUI() {
        // Update connection status
        binding.connectionStatusText.text = if (spotifyManager.isConnected()) {
            "Connected to Spotify"
        } else {
            "Not connected to Spotify"
        }
        
        // Update connect button text
        binding.connectButton.text = if (spotifyManager.isConnected()) {
            "Disconnect"
        } else {
            "Connect to Spotify"
        }
        
        // Update shake detection status
        binding.shakeStatusText.text = if (shakeDetector.isListening()) {
            "Shake detection: ON"
        } else {
            "Shake detection: OFF"
        }
        
        // Update shake detection button text
        binding.startShakeDetectionButton.text = if (shakeDetector.isListening()) {
            "Stop Shake Detection"
        } else {
            "Start Shake Detection"
        }
        
        // Enable/disable shake detection button based on Spotify connection
        binding.startShakeDetectionButton.isEnabled = spotifyManager.isConnected()
        binding.testShakeButton.isEnabled = spotifyManager.isConnected()
    }
    
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    // Spotify Connection Listener implementations
    override fun onConnected() {
        runOnUiThread {
            updateUI()
            showToast("Connected to Spotify!")
        }
    }
    
    override fun onConnectionFailed(error: Throwable) {
        runOnUiThread {
            updateUI()
            val errorMessage = when {
                error.message?.contains("CLIENT_ID not configured") == true -> 
                    "Configuration error: Please set your Spotify Client ID"
                error.message?.contains("No installed app found") == true -> 
                    "Spotify app not installed. Please install Spotify from Play Store"
                error.message?.contains("Authentication failed") == true -> 
                    "Authentication failed. Check your Spotify app settings"
                else -> "Failed to connect to Spotify: ${error.message}"
            }
            showToast(errorMessage)
            Log.e(TAG, "Spotify connection failed", error)
        }
    }
    
    override fun onDisconnected() {
        runOnUiThread {
            stopShakeDetection()
            updateUI()
            showToast("Disconnected from Spotify")
        }
    }
    
    override fun onTrackChanged(track: Track?) {
        runOnUiThread {
            binding.currentTrackText.text = if (track != null) {
                "Playing: ${track.name} by ${track.artist.name}"
            } else {
                "No track playing"
            }
        }
    }
    
    // Shake Detection Listener implementation
    override fun onShake() {
        Log.d(TAG, "Shake detected! Changing song...")
        
        runOnUiThread {
            // Show visual feedback
            binding.shakeIndicator.text = "🎵 SHAKE DETECTED! 🎵"
            binding.shakeIndicator.alpha = 1.0f
            binding.shakeIndicator.animate()
                .alpha(0.0f)
                .setDuration(2000)
                .start()
            
            showToast("Shake detected! Changing song...")
        }
        
        // Play the shake song
        lifecycleScope.launch {
            spotifyManager.playShakeSong()
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Resume shake detection if it was previously enabled
        if (spotifyManager.isConnected() && !shakeDetector.isListening()) {
            // Don't automatically start - let user control it
        }
    }
    
    override fun onPause() {
        super.onPause()
        // Keep shake detection running in background if connected
        // This allows the app to work even when in background
    }
    
    override fun onDestroy() {
        super.onDestroy()
        shakeDetector.stopListening()
        spotifyManager.disconnect()
    }
}