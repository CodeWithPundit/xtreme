package com.xtremeiptv.feature.recording.manager

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScreenRecordingManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val mediaProjectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    private var mediaProjection: MediaProjection? = null
    private var mediaRecorder: MediaRecorder? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var outputFile: File? = null
    
    private val isRecording = AtomicBoolean(false)
    private val recordingScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private val _recordingState = MutableStateFlow<RecordingState>(RecordingState.Idle)
    private val _recordingTime = MutableStateFlow(0L)
    private val _recordingSize = MutableStateFlow(0L)
    
    private var startTime: Long = 0
    private val mainHandler = Handler(Looper.getMainLooper())
    private val timeUpdateRunnable = object : Runnable {
        override fun run() {
            if (isRecording.get()) {
                _recordingTime.value = System.currentTimeMillis() - startTime
                updateRecordingSize()
                mainHandler.postDelayed(this, 1000)
            }
        }
    }
    
    fun initializeRecording(
        resultCode: Int,
        data: Intent,
        config: RecordingConfig = RecordingConfig()
    ): Boolean {
        return try {
            mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data)
            setupMediaRecorder(config)
            createVirtualDisplay(config)
            setupMediaProjectionCallback()
            
            createNotificationChannel()
            startForegroundNotification()
            
            isRecording.set(true)
            startTime = System.currentTimeMillis()
            _recordingState.value = RecordingState.Recording
            mainHandler.post(timeUpdateRunnable)
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            _recordingState.value = RecordingState.Error(e.message ?: "Failed to start recording")
            false
        }
    }
    
    fun stopRecording() {
        if (!isRecording.get()) return
        
        isRecording.set(false)
        mainHandler.removeCallbacks(timeUpdateRunnable)
        
        try {
            mediaRecorder?.apply {
                stop()
                reset()
                release()
            }
            
            virtualDisplay?.release()
            mediaProjection?.stop()
            
            mediaRecorder = null
            virtualDisplay = null
            mediaProjection = null
            
            _recordingState.value = RecordingState.Completed(outputFile?.absolutePath)
            _recordingTime.value = 0
            _recordingSize.value = 0
            
            stopForegroundNotification()
            
        } catch (e: Exception) {
            e.printStackTrace()
            _recordingState.value = RecordingState.Error(e.message ?: "Failed to stop recording")
        }
    }
    
    fun pauseRecording() {
        if (isRecording.get() && _recordingState.value == RecordingState.Recording) {
            try {
                mediaRecorder?.pause()
                _recordingState.value = RecordingState.Paused
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun resumeRecording() {
        if (isRecording.get() && _recordingState.value == RecordingState.Paused) {
            try {
                mediaRecorder?.resume()
                _recordingState.value = RecordingState.Recording
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun setupMediaRecorder(config: RecordingConfig) {
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(config.outputFormat)
            setVideoEncoder(config.videoEncoder)
            setAudioEncoder(config.audioEncoder)
            setVideoEncodingBitRate(config.videoBitrate)
            setAudioEncodingBitRate(config.audioBitrate)
            setVideoFrameRate(config.frameRate)
            setVideoSize(config.videoWidth, config.videoHeight)
            
            outputFile = createOutputFile(config)
            setOutputFile(outputFile?.absolutePath)
            
            prepare()
        }
    }
    
    private fun createVirtualDisplay(config: RecordingConfig) {
        val displayMetrics = context.resources.displayMetrics
        val density = displayMetrics.densityDpi
        
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "ScreenRecording",
            config.videoWidth,
            config.videoHeight,
            density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            mediaRecorder?.surface,
            null,
            null
        )
    }
    
    private fun setupMediaProjectionCallback() {
        mediaProjection?.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() {
                if (isRecording.get()) {
                    stopRecording()
                }
            }
        }, null)
    }
    
    private fun createOutputFile(config: RecordingConfig): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val filename = "recording_${timestamp}.${config.fileExtension}"
        
        val recordingsDir = File(context.getExternalFilesDir(null), "Recordings")
        if (!recordingsDir.exists()) {
            recordingsDir.mkdirs()
        }
        
        return File(recordingsDir, filename)
    }
    
    private fun updateRecordingSize() {
        outputFile?.let { file ->
            if (file.exists()) {
                _recordingSize.value = file.length()
            }
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                RECORDING_CHANNEL_ID,
                "Screen Recording",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows screen recording status"
                setSound(null, null)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun startForegroundNotification() {
        val notification = createRecordingNotification()
        // Start foreground service here
    }
    
    private fun stopForegroundNotification() {
        // Stop foreground service here
    }
    
    private fun createRecordingNotification(): Notification {
        val stopIntent = Intent(context, RecordingService::class.java).apply {
            action = RecordingService.ACTION_STOP_RECORDING
        }
        val stopPendingIntent = PendingIntent.getService(
            context,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(context, RECORDING_CHANNEL_ID)
            .setContentTitle("Screen Recording")
            .setContentText("Recording in progress...")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                android.R.drawable.ic_media_pause,
                "Stop",
                stopPendingIntent
            )
            .build()
    }
    
    fun getRecentRecordings(): List<File> {
        val recordingsDir = File(context.getExternalFilesDir(null), "Recordings")
        return if (recordingsDir.exists()) {
            recordingsDir.listFiles { file ->
                file.isFile && file.extension in listOf("mp4", "3gp", "webm")
            }?.sortedByDescending { it.lastModified() }?.toList() ?: emptyList()
        } else {
            emptyList()
        }
    }
    
    fun deleteRecording(file: File): Boolean {
        return if (file.exists() && file.parentFile?.name == "Recordings") {
            file.delete()
        } else {
            false
        }
    }
    
    val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()
    val recordingTime: StateFlow<Long> = _recordingTime.asStateFlow()
    val recordingSize: StateFlow<Long> = _recordingSize.asStateFlow()
    
    companion object {
        private const val RECORDING_CHANNEL_ID = "screen_recording_channel"
    }
}

sealed class RecordingState {
    object Idle : RecordingState()
    object Recording : RecordingState()
    object Paused : RecordingState()
    data class Completed(val filePath: String?) : RecordingState()
    data class Error(val message: String) : RecordingState()
}

data class RecordingConfig(
    val videoWidth: Int = 1280,
    val videoHeight: Int = 720,
    val videoBitrate: Int = 4_000_000,
    val audioBitrate: Int = 128_000,
    val frameRate: Int = 30,
    val outputFormat: Int = MediaRecorder.OutputFormat.MPEG_4,
    val videoEncoder: Int = MediaRecorder.VideoEncoder.H264,
    val audioEncoder: Int = MediaRecorder.AudioEncoder.AAC,
    val fileExtension: String = "mp4"
)
