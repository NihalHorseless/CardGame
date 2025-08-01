package io.github.nihalhorseless.eternalglory.util

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import io.github.nihalhorseless.eternalglory.BuildConfig
import kotlinx.coroutines.CoroutineExceptionHandler
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CrashHandler private constructor(private val context: Context) : Thread.UncaughtExceptionHandler {

    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

    companion object {
        private const val TAG = "CrashHandler"
        @SuppressLint("StaticFieldLeak")
        private var instance: CrashHandler? = null

        fun init(context: Context) {
            if (instance == null) {
                instance = CrashHandler(context.applicationContext)
                Thread.setDefaultUncaughtExceptionHandler(instance)
            }
        }
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            saveCrashLog(throwable)
            // In production, you'd send this to a crash reporting service
        } catch (_: Exception) {

        }

        // Let the default handler deal with it
        defaultHandler?.uncaughtException(thread, throwable)
    }

    private fun saveCrashLog(throwable: Throwable) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
        val filename = "crash_$timestamp.txt"

        val stackTrace = StringWriter()
        throwable.printStackTrace(PrintWriter(stackTrace))

        val crashLog = buildString {
            appendLine("=== CRASH LOG ===")
            appendLine("Timestamp: $timestamp")
            appendLine("Device: ${Build.MODEL}")
            appendLine("Android Version: ${Build.VERSION.RELEASE}")
            appendLine("App Version: ${BuildConfig.VERSION_NAME}")
            appendLine("\nStack Trace:")
            appendLine(stackTrace.toString())
        }

        // Save to internal storage
        try {
            File(context.filesDir, "crashes").apply {
                if (!exists()) mkdirs()
                File(this, filename).writeText(crashLog)
            }
        } catch (_: Exception) {
        }
    }
}

// Global Coroutine Exception Handler
val globalCoroutineExceptionHandler = CoroutineExceptionHandler { context, throwable ->
    // Send to crash reporting service
}