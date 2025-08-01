# Spotify Shaker

An Android application that changes the currently playing Spotify song when you shake your phone!

## Features

- 🎵 Shake detection using device accelerometer
- 🎧 Spotify integration for music control via Web API
- 📱 Simple and intuitive user interface
- 🔄 Real-time track information display
- 🎯 Customizable shake sensitivity

## Prerequisites

Before you begin, ensure you have:

1. **Android Studio** installed (latest version recommended)
2. **Spotify Developer Account** - [Sign up here](https://developer.spotify.com/)
3. **Spotify Mobile App** installed on your test device
4. **Android device with accelerometer** (physical device required for shake detection)
5. **Spotify Premium** (required for playback control)

## Setup Instructions

### 1. Spotify App Registration

1. Go to [Spotify Developer Dashboard](https://developer.spotify.com/dashboard/)
2. Click "Create App"
3. Fill in the app details:
   - **App Name**: Spotify Shaker
   - **App Description**: Android app that changes songs on shake
4. Add the redirect URI: `spotify-shaker-auth://callback`
5. Copy your **Client ID** (you'll need this later)

### 2. Configure the App

1. Open `app/src/main/java/com/example/spotifyshaker/SpotifyManager.kt`
2. Replace `"your_spotify_client_id_here"` with your actual Spotify Client ID:
   ```kotlin
   private const val CLIENT_ID = "your_actual_client_id_here"
   ```
3. (Optional) Replace the default shake song URI with your preferred song:
   ```kotlin
   private const val DEFAULT_SHAKE_SONG_URI = "spotify:track:your_track_id_here"
   ```

### 3. Build and Install

1. Open the project in Android Studio
2. The project will automatically download the Spotify Auth Library from Maven
3. Connect your Android device via USB (enable Developer Options and USB Debugging)
4. Build and run the project on your device

## How to Use

1. **Install Spotify**: Make sure the Spotify mobile app is installed and you're logged in
2. **Start Playing Music**: Open Spotify and start playing any song (this is required for playback control)
3. **Open Spotify Shaker**: Launch the app on your device
4. **Connect to Spotify**: Tap "Connect to Spotify" and authorize the app
5. **Start Shake Detection**: Tap "Start Shake Detection"
6. **Shake Away!**: Shake your phone to change the currently playing song

## How It Works

### Shake Detection
- Uses the device's accelerometer to detect shake gestures
- Calculates G-force magnitude from X, Y, Z acceleration values
- Triggers when acceleration exceeds threshold (12.0 G-force by default)
- Includes debouncing to prevent multiple rapid triggers

### Spotify Integration
- Uses Spotify Auth Library for OAuth 2.0 authentication
- Integrates with Spotify Web API for playback control
- Monitors current track information via API polling
- Plays specified track when shake is detected

## Customization

### Adjust Shake Sensitivity
In `ShakeDetector.kt`, modify the threshold:
```kotlin
private const val SHAKE_THRESHOLD = 12.0f // Increase for less sensitivity
```

### Change the Shake Song
In `SpotifyManager.kt`, update the song URI:
```kotlin
private const val DEFAULT_SHAKE_SONG_URI = "spotify:track:your_track_id_here"
```

To find a Spotify track URI:
1. Open Spotify desktop/mobile app
2. Right-click on a song → Share → Copy Spotify URI
3. Use the format: `spotify:track:TRACK_ID`

### Modify Shake Cooldown
In `ShakeDetector.kt`, adjust the minimum time between shakes:
```kotlin
private const val SHAKE_TIME_THRESHOLD = 500 // milliseconds
```

## Troubleshooting

### Common Issues

**"Failed to connect to Spotify"**
- Ensure Spotify app is installed and you're logged in
- Check that your Client ID is correct
- Verify the redirect URI matches your Spotify app settings

**"No active device found"**
- Open Spotify app and start playing music first
- The Web API requires an active playback session to control

**"Accelerometer not available"**
- This app requires a physical device with an accelerometer
- Emulators typically don't support shake detection

**Shake not detected**
- Try adjusting the shake threshold in `ShakeDetector.kt`
- Ensure shake detection is enabled in the app
- Check device logs for sensor events

**Song doesn't change**
- Make sure you're connected to Spotify
- Verify that music is actively playing in Spotify
- Check that the track URI is valid
- Ensure you have Spotify Premium (required for playback control)

### Debug Logs

Enable debug logging by filtering LogCat for these tags:
- `SpotifyManager`: Spotify connection and API events
- `ShakeDetector`: Accelerometer and shake detection events
- `MainActivity`: General app state and UI events

## Dependencies

- **Spotify Auth Library**: OAuth 2.0 authentication (`com.spotify.android:auth:2.1.1`)
- **OkHttp**: HTTP client for Spotify Web API calls
- **AndroidX Libraries**: Modern Android development
- **Material Design Components**: UI elements
- **Kotlin Coroutines**: Asynchronous operations

## Permissions

The app requires these permissions:
- `INTERNET`: Spotify API communication
- `ACCESS_NETWORK_STATE`: Network connectivity checks
- `WAKE_LOCK`: Keep device awake during playback

## API Limitations

- **Requires Spotify Premium** for playback control
- Limited to 25 API calls per second
- User must have Spotify app installed
- Only works with Spotify tracks (not local files)
- Requires active playback session for device control

## Modern Spotify SDK Approach

This app uses the **Spotify Auth Library** (available via Maven) combined with the **Spotify Web API**. This is the recommended approach as of 2024, replacing the older App Remote SDK method that required manual AAR file downloads.

### Why This Approach?
- ✅ No manual SDK file downloads required
- ✅ Available through standard Maven repositories
- ✅ More reliable authentication flow
- ✅ Direct Web API integration
- ✅ Better long-term support

## Contributing

Feel free to submit issues and enhancement requests!

## License

This project is for educational purposes. Please review Spotify's Terms of Service before distributing.

---

**Note**: This app is not affiliated with Spotify. Spotify is a trademark of Spotify AB. 



