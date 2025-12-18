package com.charles.messenger.common.util

import android.content.Context
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsInitializer @Inject constructor() {
    fun init(context: Context) {
        // No-op for noAnalytics flavor
    }
}
