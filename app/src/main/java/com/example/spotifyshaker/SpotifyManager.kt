package com.example.spotifyshaker

import android.content.Context
import android.content.Intent
import android.util.Log
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class SpotifyManager(private val context: Context) {
    
    companion object {
        private const val TAG = "SpotifyManager"
        private const val CLIENT_ID = "your_spotify_client_id_here" // Replace with your Spotify Client ID
        private const val REDIRECT_URI = "spotify-shaker-auth://callback"
        private const val REQUEST_CODE = 1337
        
        // Default song URI to play when shaking (replace with your desired song)
        private const val DEFAULT_SHAKE_SONG_URI = "spotify:track:4iV5W9uYEdYUVa79Axb7Rh" // Example: Never Gonna Give You Up
        
        // Spotify Web API endpoints
        private const val SPOTIFY_API_BASE = "https://api.spotify.com/v1"
        private const val CURRENT_PLAYBACK_URL = "$SPOTIFY_API_BASE/me/player"
        private const val PLAY_URL = "$SPOTIFY_API_BASE/me/player/play"
    }
    
    private var accessToken: String? = null
    private var isConnected = false
    private val httpClient = OkHttpClient()
    
    interface SpotifyConnectionListener {
        fun onConnected()
        fun onConnectionFailed(error: Throwable)
        fun onDisconnected()
        fun onTrackChanged(trackName: String?, artistName: String?)
    }
    
    private var connectionListener: SpotifyConnectionListener? = null
    
    fun setConnectionListener(listener: SpotifyConnectionListener) {
        this.connectionListener = listener
    }
    
    fun startAuth(activity: android.app.Activity) {
        val builder = AuthorizationRequest.Builder(
            CLIENT_ID,
            AuthorizationResponse.Type.TOKEN,
            REDIRECT_URI
        )
        
        builder.setScopes(arrayOf(
            "user-read-playback-state",
            "user-modify-playback-state",
            "user-read-currently-playing"
        ))
        
        val request = builder.build()
        AuthorizationClient.openLoginActivity(activity, REQUEST_CODE, request)
    }
    
    fun handleAuthResponse(intent: Intent) {
        val response = AuthorizationClient.getResponse(REQUEST_CODE, intent)
        
        when (response.type) {
            AuthorizationResponse.Type.TOKEN -> {
                accessToken = response.accessToken
                isConnected = true
                Log.d(TAG, "Authentication successful!")
                connectionListener?.onConnected()
                
                // Start monitoring current playback
                startPlaybackMonitoring()
            }
            AuthorizationResponse.Type.ERROR -> {
                Log.e(TAG, "Authentication error: ${response.error}")
                isConnected = false
                connectionListener?.onConnectionFailed(Exception(response.error))
            }
            else -> {
                Log.d(TAG, "Authentication cancelled or other response: ${response.type}")
                isConnected = false
            }
        }
    }
    
    private fun startPlaybackMonitoring() {
        CoroutineScope(Dispatchers.IO).launch {
            while (isConnected) {
                getCurrentPlayback()
                kotlinx.coroutines.delay(5000) // Check every 5 seconds
            }
        }
    }
    
    private suspend fun getCurrentPlayback() {
        if (!isConnected || accessToken == null) return
        
        try {
            val request = Request.Builder()
                .url(CURRENT_PLAYBACK_URL)
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
            
            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    responseBody?.let { parseCurrentTrack(it) }
                } else if (response.code == 401) {
                    // Token expired
                    withContext(Dispatchers.Main) {
                        disconnect()
                        connectionListener?.onConnectionFailed(Exception("Token expired"))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current playback", e)
        }
    }
    
    private suspend fun parseCurrentTrack(responseBody: String) {
        try {
            val json = JSONObject(responseBody)
            val item = json.optJSONObject("item")
            
            val trackName = item?.optString("name")
            val artistsArray = item?.optJSONArray("artists")
            val artistName = if (artistsArray != null && artistsArray.length() > 0) {
                artistsArray.getJSONObject(0).optString("name")
            } else null
            
            withContext(Dispatchers.Main) {
                connectionListener?.onTrackChanged(trackName, artistName)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing track info", e)
        }
    }
    
    fun playShakeSong() {
        playCustomSong(DEFAULT_SHAKE_SONG_URI)
    }
    
    fun playCustomSong(uri: String) {
        if (!isConnected || accessToken == null) {
            Log.w(TAG, "Not connected to Spotify")
            return
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val json = JSONObject().apply {
                    put("uris", org.json.JSONArray().put(uri))
                }
                
                val mediaType = "application/json; charset=utf-8".toMediaType()
                val requestBody = json.toString().toRequestBody(mediaType)
                
                val request = Request.Builder()
                    .url(PLAY_URL)
                    .addHeader("Authorization", "Bearer $accessToken")
                    .put(requestBody)
                    .build()
                
                httpClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        Log.d(TAG, "Playing song: $uri")
                    } else {
                        Log.e(TAG, "Failed to play song: ${response.code} - ${response.message}")
                        if (response.code == 404) {
                            Log.e(TAG, "No active device found. Please start Spotify and begin playing music first.")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error playing song", e)
            }
        }
    }
    
    fun pause() {
        sendPlayerCommand("pause", "POST")
    }
    
    fun resume() {
        sendPlayerCommand("play", "PUT")
    }
    
    fun skipNext() {
        sendPlayerCommand("next", "POST")
    }
    
    fun skipPrevious() {
        sendPlayerCommand("previous", "POST")
    }
    
    private fun sendPlayerCommand(command: String, method: String) {
        if (!isConnected || accessToken == null) {
            Log.w(TAG, "Not connected to Spotify")
            return
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = "$SPOTIFY_API_BASE/me/player/$command"
                val requestBuilder = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer $accessToken")
                
                val request = when (method) {
                    "POST" -> requestBuilder.post("".toRequestBody()).build()
                    "PUT" -> requestBuilder.put("".toRequestBody()).build()
                    else -> requestBuilder.build()
                }
                
                httpClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        Log.d(TAG, "Command $command executed successfully")
                    } else {
                        Log.e(TAG, "Failed to execute command $command: ${response.code}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error executing command $command", e)
            }
        }
    }
    
    fun isConnected(): Boolean {
        return isConnected && accessToken != null
    }
    
    fun disconnect() {
        isConnected = false
        accessToken = null
        connectionListener?.onDisconnected()
        Log.d(TAG, "Disconnected from Spotify")
    }
}