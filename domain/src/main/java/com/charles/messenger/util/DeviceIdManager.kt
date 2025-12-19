/*
 * Copyright (C) 2024 Moez Bhatti <charles.bhatti@gmail.com>
 *
 * This file is part of messenger.
 *
 * messenger is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * messenger is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with messenger.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.charles.messenger.util

import android.content.Context
import android.provider.Settings
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages device identification for tracking trial usage across app reinstalls.
 * Uses Android ID which persists across app reinstalls (but not factory resets).
 */
@Singleton
class DeviceIdManager @Inject constructor(
    private val context: Context
) {
    /**
     * Gets a stable device identifier that persists across app reinstalls.
     * This is a hash of the Android ID to provide some privacy protection.
     */
    fun getDeviceId(): String {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        // Hash the Android ID for privacy and to ensure it's a valid format
        return if (androidId != null && androidId.isNotEmpty()) {
            hashString(androidId)
        } else {
            // Fallback: use a combination of other identifiers if Android ID is unavailable
            hashString("fallback_${android.os.Build.SERIAL}_${android.os.Build.MODEL}")
        }
    }

    private fun hashString(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}

