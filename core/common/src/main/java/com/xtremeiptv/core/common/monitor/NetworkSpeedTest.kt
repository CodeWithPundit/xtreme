package com.xtremeiptv.core.common.monitor

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkSpeedTest @Inject constructor() {

    private val _downloadSpeed = MutableStateFlow<Double?>(null)
    val downloadSpeed: StateFlow<Double?> = _downloadSpeed.asStateFlow()

    private val _uploadSpeed = MutableStateFlow<Double?>(null)
    val uploadSpeed: StateFlow<Double?> = _uploadSpeed.asStateFlow()

    private val _ping = MutableStateFlow<Int?>(null)
    val ping: StateFlow<Int?> = _ping.asStateFlow()

    private val _isTesting = MutableStateFlow(false)
    val isTesting: StateFlow<Boolean> = _isTesting.asStateFlow()

    private val testScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun startSpeedTest() {
        if (_isTesting.value) return

        _isTesting.value = true
        _downloadSpeed.value = null
        _uploadSpeed.value = null
        _ping.value = null

        testScope.launch {
            try {
                // Test ping
                measurePing()

                // Test download speed
                measureDownloadSpeed()

                // Test upload speed (if needed)
                // measureUploadSpeed()

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isTesting.value = false
            }
        }
    }

    private suspend fun measurePing() {
        val startTime = System.currentTimeMillis()
        try {
            val url = URL("https://www.google.com")
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.connect()
            connection.disconnect()

            val pingTime = System.currentTimeMillis() - startTime
            _ping.value = pingTime.toInt()
        } catch (e: Exception) {
            _ping.value = -1
        }
    }

    private suspend fun measureDownloadSpeed() {
        val testFileUrl = "https://speedtest.tele2.net/10MB.zip" // 10MB test file
        val bytesToRead = 1024 * 1024 // 1MB for test

        try {
            val url = URL(testFileUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 10000
            connection.readTimeout = 30000
            connection.connect()

            val contentLength = connection.contentLength
            val inputStream = connection.inputStream

            val startTime = System.currentTimeMillis()
            var totalBytesRead = 0
            val buffer = ByteArray(4096)

            while (totalBytesRead < bytesToRead) {
                val bytesRead = inputStream.read(buffer)
                if (bytesRead == -1) break
                totalBytesRead += bytesRead
            }

            val endTime = System.currentTimeMillis()
            val durationSeconds = (endTime - startTime) / 1000.0

            val speedMbps = (totalBytesRead * 8) / (durationSeconds * 1_000_000)

            _downloadSpeed.value = speedMbps

            inputStream.close()
            connection.disconnect()

        } catch (e: Exception) {
            e.printStackTrace()
            _downloadSpeed.value = -1.0
        }
    }

    fun getSpeedGrade(speedMbps: Double): String {
        return when {
            speedMbps < 1 -> "Very Poor"
            speedMbps < 3 -> "Poor"
            speedMbps < 10 -> "Fair"
            speedMbps < 25 -> "Good"
            speedMbps < 50 -> "Very Good"
            else -> "Excellent"
        }
    }

    fun getRecommendedQuality(speedMbps: Double): String {
        return when {
            speedMbps < 1 -> "240p"
            speedMbps < 2 -> "360p"
            speedMbps < 4 -> "480p"
            speedMbps < 8 -> "720p"
            speedMbps < 16 -> "1080p"
            else -> "4K"
        }
    }

    fun cancelTest() {
        testScope.coroutineContext.cancelChildren()
        _isTesting.value = false
    }
}
