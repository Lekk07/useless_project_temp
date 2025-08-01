package com.example.spotifyshaker

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlin.math.sqrt

class ShakeDetector(private val context: Context) : SensorEventListener {
    
    companion object {
        private const val TAG = "ShakeDetector"
        private const val SHAKE_THRESHOLD = 12.0f // Acceleration threshold for shake detection
        private const val SHAKE_TIME_THRESHOLD = 500 // Minimum time between shakes in milliseconds
    }
    
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var isListening = false
    private var lastShakeTime: Long = 0
    
    interface OnShakeListener {
        fun onShake()
    }
    
    private var shakeListener: OnShakeListener? = null
    
    init {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        
        if (accelerometer == null) {
            Log.w(TAG, "Accelerometer not available on this device")
        }
    }
    
    fun setOnShakeListener(listener: OnShakeListener) {
        this.shakeListener = listener
    }
    
    fun startListening() {
        if (accelerometer != null && !isListening) {
            sensorManager?.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_UI
            )
            isListening = true
            Log.d(TAG, "Started listening for shake gestures")
        } else {
            Log.w(TAG, "Cannot start listening - accelerometer not available or already listening")
        }
    }
    
    fun stopListening() {
        if (isListening) {
            sensorManager?.unregisterListener(this)
            isListening = false
            Log.d(TAG, "Stopped listening for shake gestures")
        }
    }
    
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            
            // Calculate the acceleration magnitude (removing gravity)
            val gX = x / SensorManager.GRAVITY_EARTH
            val gY = y / SensorManager.GRAVITY_EARTH
            val gZ = z / SensorManager.GRAVITY_EARTH
            
            // Calculate the total acceleration
            val gForce = sqrt(gX * gX + gY * gY + gZ * gZ)
            
            // Check if the acceleration exceeds our threshold
            if (gForce > SHAKE_THRESHOLD) {
                val currentTime = System.currentTimeMillis()
                
                // Prevent multiple shake detections in quick succession
                if (currentTime - lastShakeTime > SHAKE_TIME_THRESHOLD) {
                    lastShakeTime = currentTime
                    Log.d(TAG, "Shake detected! G-force: $gForce")
                    shakeListener?.onShake()
                }
            }
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used for this implementation
    }
    
    fun isListening(): Boolean {
        return isListening
    }
    
    fun isAccelerometerAvailable(): Boolean {
        return accelerometer != null
    }
}