/*
 * Copyright (C) 2024 Charles Hartmann
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.charles.messenger.common.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.charles.messenger.R
import com.charles.messenger.feature.settings.ai.AiSettingsController
import com.charles.messenger.util.Preferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiAutoReplyNotification @Inject constructor(
    private val context: Context,
    private val prefs: Preferences
) {

    companion object {
        private const val CHANNEL_ID = "ai_auto_reply"
        private const val NOTIFICATION_ID = 9001
    }

    private val notificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.ai_auto_reply_notification_title),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Persistent notification when AI auto-reply is active"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun show() {
        // Create intent to open AI settings
        val settingsIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            setPackage(context.packageName)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            settingsIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        // Create disable action
        val disableIntent = Intent(context, DisableAutoReplyReceiver::class.java)
        val disablePendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            disableIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_smart_reply_24dp)
            .setContentTitle(context.getString(R.string.ai_auto_reply_notification_title))
            .setContentText(context.getString(R.string.ai_auto_reply_notification_text))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_cancel_black_24dp,
                context.getString(R.string.ai_auto_reply_notification_action),
                disablePendingIntent
            )
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun dismiss() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

    fun updateIfNeeded() {
        if (prefs.aiAutoReplyToAll.get()) {
            show()
        } else {
            dismiss()
        }
    }
}
