package com.example.telexerrorhandler

import android.app.Application
import com.example.exceptionlogger.ExceptionLogger

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val apiUrl = "https://ping.telex.im/v1/webhooks/01952cfb-0b6b-74af-8a2b-a2366321cdb9"

        // Initialize with apiUrl and a default username
        ExceptionLogger.initialize(
            apiUrl = apiUrl,
            username = "Checking App"  // You can change this to any default username you prefer
        )
    }
}