package com.xtremeiptv.core.common.analytics

import android.app.Application
import java.io.PrintWriter
import java.io.StringWriter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CrashReporter @Inject constructor(
    private val application: Application,
    private val analyticsManager: AnalyticsManager
) {

    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

    fun initialize() {
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            handleUncaughtException(thread, throwable)
        }
    }

    private fun handleUncaughtException(thread: Thread, throwable: Throwable) {
        try {
            // Log crash
            val stackTrace = StringWriter().apply {
                throwable.printStackTrace(PrintWriter(this))
            }.toString()

            analyticsManager.logError("crash", throwable.message ?: "Unknown error")

            // Save crash to file
            saveCrashReport(throwable, stackTrace)

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            // Pass to default handler
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun saveCrashReport(throwable: Throwable, stackTrace: String) {
        try {
            val crashFile = java.io.File(application.filesDir, "crash_reports.txt")
            val timestamp = System.currentTimeMillis()
            val report = """
                
                ===== Crash Report $timestamp =====
                Exception: ${throwable.javaClass.simpleName}
                Message: ${throwable.message}
                Stack Trace:
                $stackTrace
                ===================================
                
            """.trimIndent()

            crashFile.appendText(report)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun logNonFatalException(throwable: Throwable) {
        analyticsManager.logError("non_fatal", throwable.message ?: "Unknown error")
    }
}
