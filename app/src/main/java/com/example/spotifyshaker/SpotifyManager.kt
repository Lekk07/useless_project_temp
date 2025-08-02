package com.example.spotifyshaker

import android.content.Context
import android.util.Log
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SpotifyManager(private val context: Context) {
    
    companion object {
        private const val TAG = "SpotifyManager"
        // Spotify Client ID - configured from web app
        private const val CLIENT_ID = "89c4fd37aff24494b0f20708273b4bde"
        private const val REDIRECT_URI = "spotify-shaker-auth://callback"
        
        // Default song URI to play when shaking (replace with your desired song)
        private const val DEFAULT_SHAKE_SONG_URI = "spotify:track:4iV5W9uYEdYUVa79Axb7Rh" // Example: Never Gonna Give You Up
    }
    
    private var spotifyAppRemote: SpotifyAppRemote? = null
    private var currentTrack: Track? = null
    private var isConnected = false
    
    interface SpotifyConnectionListener {
        fun onConnected()
        fun onConnectionFailed(error: Throwable)
        fun onDisconnected()
        fun onTrackChanged(track: Track?)
    }
    
    private var connectionListener: SpotifyConnectionListener? = null
    
    fun setConnectionListener(listener: SpotifyConnectionListener) {
        this.connectionListener = listener
    }
    
    fun connect() {
        // Validate configuration before attempting connection
        if (CLIENT_ID == "your_spotify_client_id_here") {
            val configError = Exception("CLIENT_ID not configured. Please set your Spotify Client ID in SpotifyManager.kt")
            Log.e(TAG, "Configuration error: CLIENT_ID is not set")
            connectionListener?.onConnectionFailed(configError)
            return
        }
        
        Log.d(TAG, "Attempting to connect to Spotify with Client ID: ${CLIENT_ID.take(8)}...")
        Log.d(TAG, "Redirect URI: $REDIRECT_URI")
        
        val connectionParams = ConnectionParams.Builder(CLIENT_ID)
            .setRedirectUri(REDIRECT_URI)
            .showAuthView(true)
            .build()
        
        SpotifyAppRemote.connect(context, connectionParams, object : Connector.ConnectionListener {
            override fun onConnected(appRemote: SpotifyAppRemote) {
                spotifyAppRemote = appRemote
                isConnected = true
                Log.d(TAG, "Successfully connected to Spotify!")
                
                // Subscribe to player state
                subscribeToPlayerState()
                
                connectionListener?.onConnected()
            }
            
            override fun onFailure(error: Throwable) {
                Log.e(TAG, "Failed to connect to Spotify", error)
                Log.e(TAG, "Error details: ${error.message}")
                Log.e(TAG, "Check: 1) Spotify app is installed 2) Client ID is correct 3) Redirect URI matches Spotify app settings")
                isConnected = false
                connectionListener?.onConnectionFailed(error)
            }
        })
    }
    
    private fun subscribeToPlayerState() {
        spotifyAppRemote?.playerApi?.subscribeToPlayerState()?.setEventCallback { playerState ->
            currentTrack = playerState.track
            Log.d(TAG, "Current track: ${currentTrack?.name} by ${currentTrack?.artist?.name}")
            connectionListener?.onTrackChanged(currentTrack)
        }
    }
    
    fun playShakeSong() {
        if (!isConnected) {
            Log.w(TAG, "Not connected to Spotify")
            return
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                spotifyAppRemote?.playerApi?.play(DEFAULT_SHAKE_SONG_URI)
                Log.d(TAG, "Playing shake song: $DEFAULT_SHAKE_SONG_URI")
            } catch (e: Exception) {
                Log.e(TAG, "Error playing shake song", e)
            }
        }
    }
    
    fun playCustomSong(uri: String) {
        if (!isConnected) {
            Log.w(TAG, "Not connected to Spotify")
            return
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                spotifyAppRemote?.playerApi?.play(uri)
                Log.d(TAG, "Playing custom song: $uri")
            } catch (e: Exception) {
                Log.e(TAG, "Error playing custom song", e)
            }
        }
    }
    
    fun pause() {
        spotifyAppRemote?.playerApi?.pause()
    }
    
    fun resume() {
        spotifyAppRemote?.playerApi?.resume()
    }
    
    fun skipNext() {
        spotifyAppRemote?.playerApi?.skipNext()
    }
    
    fun skipPrevious() {
        spotifyAppRemote?.playerApi?.skipPrevious()
    }
    
    fun getCurrentTrack(): Track? {
        return currentTrack
    }
    
    fun isConnected(): Boolean {
        return isConnected
    }
    
    fun disconnect() {
        if (isConnected) {
            SpotifyAppRemote.disconnect(spotifyAppRemote)
            isConnected = false
            spotifyAppRemote = null
            connectionListener?.onDisconnected()
            Log.d(TAG, "Disconnected from Spotify")
        }
    }
}