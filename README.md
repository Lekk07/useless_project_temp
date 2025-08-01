# Spotify Shaker

An Android application that changes the currently playing Spotify song when you shake your phone!

## Features

- 🎵 Shake detection using device accelerometer
- 🎧 Spotify integration for music control
- 📱 Simple and intuitive user interface
- 🔄 Real-time track information display
- 🎯 Customizable shake sensitivity

## Prerequisites

Before you begin, ensure you have:

1. **Android Studio** installed (latest version recommended)
2. **Spotify Developer Account** - [Sign up here](https://developer.spotify.com/)
3. **Spotify Mobile App** installed on your test device
4. **Android device with accelerometer** (physical device required for shake detection)

## Setup Instructions

### 1. Spotify App Registration

1. Go to [Spotify Developer Dashboard](https://developer.spotify.com/dashboard/)
2. Click "Create an App"
3. Fill in the app details:
   - **App Name**: Spotify Shaker
   - **App Description**: Android app that changes songs on shake
4. Add the redirect URI: `spotify-shaker-auth://callback`
5. Copy your **Client ID** (you'll need this later)

### 2. Download Spotify SDK

1. Download the Spotify Android SDK from [Spotify's GitHub releases](https://github.com/spotify/android-sdk/releases)
2. Extract the `spotify-app-remote-release-0.8.0.aar` file
3. Place it in the `app/libs/` directory of your project

### 3. Configure the App

1. Open `app/src/main/java/com/example/spotifyshaker/SpotifyManager.kt`
2. Replace `"your_spotify_client_id_here"` with your actual Spotify Client ID:
   ```kotlin
   private const val CLIENT_ID = "your_actual_client_id_here"
   ```
3. (Optional) Replace the default shake song URI with your preferred song:
   ```kotlin
   private const val DEFAULT_SHAKE_SONG_URI = "spotify:track:your_track_id_here"
   ```

### 4. Build and Install

1. Open the project in Android Studio
2. Connect your Android device via USB (enable Developer Options and USB Debugging)
3. Build and run the project on your device

## How to Use

1. **Install Spotify**: Make sure the Spotify mobile app is installed and you're logged in
2. **Open Spotify Shaker**: Launch the app on your device
3. **Connect to Spotify**: Tap "Connect to Spotify" and authorize the app
4. **Start Shake Detection**: Tap "Start Shake Detection"
5. **Shake Away!**: Shake your phone to change the currently playing song

## How It Works

### Shake Detection
- Uses the device's accelerometer to detect shake gestures
- Calculates G-force magnitude from X, Y, Z acceleration values
- Triggers when acceleration exceeds threshold (12.0 G-force by default)
- Includes debouncing to prevent multiple rapid triggers

### Spotify Integration
- Uses Spotify App Remote SDK for real-time playback control
- Authenticates via OAuth 2.0 flow
- Subscribes to player state for current track information
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

**"Accelerometer not available"**
- This app requires a physical device with an accelerometer
- Emulators typically don't support shake detection

**Shake not detected**
- Try adjusting the shake threshold in `ShakeDetector.kt`
- Ensure shake detection is enabled in the app
- Check device logs for sensor events

**Song doesn't change**
- Make sure you're connected to Spotify
- Verify that music is playing in Spotify
- Check that the track URI is valid

### Debug Logs

Enable debug logging by filtering LogCat for these tags:
- `SpotifyManager`: Spotify connection and playback events
- `ShakeDetector`: Accelerometer and shake detection events
- `MainActivity`: General app state and UI events

## Dependencies

- **Spotify App Remote SDK**: Music playback control
- **AndroidX Libraries**: Modern Android development
- **Material Design Components**: UI elements
- **Kotlin Coroutines**: Asynchronous operations

## Permissions

The app requires these permissions:
- `INTERNET`: Spotify API communication
- `ACCESS_NETWORK_STATE`: Network connectivity checks
- `WAKE_LOCK`: Keep device awake during playback
- `MODIFY_PLAYBACK`: Spotify playback control

## API Limitations

- Requires Spotify Premium for playback control
- Limited to 25 API calls per second
- User must have Spotify app installed
- Only works with Spotify tracks (not local files)

## Contributing

Feel free to submit issues and enhancement requests!

## License

This project is for educational purposes. Please review Spotify's Terms of Service before distributing.

---

**Note**: This app is not affiliated with Spotify. Spotify is a trademark of Spotify AB. 



