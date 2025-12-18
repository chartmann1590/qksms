package com.charles.messenger.common.util

import android.content.Context
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsInitializer @Inject constructor() {
    fun init(context: Context) {
        // Initialize Firebase Analytics
        try {
            Firebase.analytics
        } catch (e: Exception) {
            // Ignore if missing google-services.json or other init issues
        }
    }
}
