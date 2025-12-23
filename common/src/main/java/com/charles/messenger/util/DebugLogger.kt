/*
 * Debug logging utility for runtime debugging
 */
package com.charles.messenger.util

import android.content.Context
import android.os.Environment
import android.util.Log
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter

object DebugLogger {
    private var logFile: File? = null
    private const val LOG_FILE_NAME = "debug.log"
    
    fun init(context: Context) {
        try {
            val externalDir = context.getExternalFilesDir(null)
            logFile = File(externalDir, LOG_FILE_NAME)
            logFile?.parentFile?.mkdirs()
        } catch (e: Exception) {
            Log.e("DebugLogger", "Failed to init debug logger", e)
        }
    }

    fun log(
        location: String,
        message: String,
        data: Map<String, Any?> = emptyMap(),
        sessionId: String = "debug-session",
        runId: String = "run1",
        hypothesisId: String? = null
    ) {
        try {
            val logEntry = JSONObject().apply {
                put("id", "log_${System.currentTimeMillis()}_${hashCode()}")
                put("timestamp", System.currentTimeMillis())
                put("location", location)
                put("message", message)
                put("sessionId", sessionId)
                put("runId", runId)
                if (hypothesisId != null) put("hypothesisId", hypothesisId)
                if (data.isNotEmpty()) {
                    val dataObj = JSONObject()
                    data.forEach { (k, v) -> dataObj.put(k, v?.toString() ?: "null") }
                    put("data", dataObj)
                }
            }

            logFile?.let { file ->
                PrintWriter(FileWriter(file, true), true).use { writer ->
                    writer.println(logEntry.toString())
                }
            } ?: run {
                // Fallback to logcat if file not initialized
                Log.d("DebugLogger", logEntry.toString())
            }
        } catch (e: Exception) {
            Log.e("DebugLogger", "Failed to write debug log", e)
        }
    }
}

