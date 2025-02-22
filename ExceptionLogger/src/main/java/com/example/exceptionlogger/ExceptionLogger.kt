package com.example.exceptionlogger

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ExceptionLogger private constructor(
    private val apiUrl: String,
    private var username: String = "default_user",
    private var enableGlobalHandler: Boolean = true
) {
    companion object {
        private var instance: ExceptionLogger? = null

        fun initialize(
            apiUrl: String,
            username: String = "default_user",
            enableGlobalHandler: Boolean = true
        ) {
            instance = ExceptionLogger(apiUrl, username, enableGlobalHandler).also {
                if (enableGlobalHandler) {
                    it.setDefaultUncaughtExceptionHandler()
                }
            }
        }

        fun logException(
            exception: Throwable,
            source: String? = null,
            method: String? = null,
            additionalInfo: Map<String, Any>? = null
        ) {
            instance?.sendLogToServer(exception, source, method, additionalInfo)
        }

        fun updateUsername(newUsername: String) {
            instance?.username = newUsername
        }
    }

    private fun setDefaultUncaughtExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            sendLogToServer(throwable, "GlobalExceptionHandler", "uncaughtException")
        }
    }

    private fun sendLogToServer(
        throwable: Throwable,
        source: String? = null,
        method: String? = null,
        additionalInfo: Map<String, Any>? = null
    ) {
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()

        // Get a brief stack trace (first few lines)
        val briefStackTrace = throwable.stackTrace
            .take(3)
            .joinToString("\n") { "    at $it" }

        // Build the error message with context
        val contextInfo = buildString {
            append(throwable.message ?: "No message")
            append("\n")
            if (source != null) append("\nSource: $source")
            if (method != null) append("\nMethod: $method")
            append("\n$briefStackTrace")
            if (!additionalInfo.isNullOrEmpty()) {
                append("\nAdditional Info:")
                additionalInfo.forEach { (key, value) ->
                    append("\n  $key: $value")
                }
            }
        }

        // Construct the JSON body according to the specified format
        val jsonBody = JSONObject().apply {
            put("event_name", throwable::class.java.simpleName)
            put("message", contextInfo)
            put("status", "error")
            put("username", username)
        }

        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(apiUrl)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ExceptionLogger", "Error sending log: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.e("ExceptionLogger", "Failed to send log: ${response.code} - ${response.message}")
                }
            }
        })
    }
}