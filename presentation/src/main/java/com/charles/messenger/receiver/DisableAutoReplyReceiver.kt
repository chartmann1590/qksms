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
package com.charles.messenger.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.charles.messenger.R
import com.charles.messenger.common.util.AiAutoReplyNotification
import com.charles.messenger.util.Preferences
import dagger.android.AndroidInjection
import timber.log.Timber
import javax.inject.Inject

class DisableAutoReplyReceiver : BroadcastReceiver() {

    @Inject lateinit var prefs: Preferences
    @Inject lateinit var notification: AiAutoReplyNotification

    override fun onReceive(context: Context, intent: Intent) {
        try {
            AndroidInjection.inject(this, context)
        } catch (e: Exception) {
            Timber.e(e, "Failed to inject dependencies")
            return
        }

        Timber.d("Disabling AI auto-reply")

        // Disable auto-reply
        prefs.aiAutoReplyToAll.set(false)

        // Dismiss notification
        notification.dismiss()

        // Show toast confirmation
        Toast.makeText(
            context,
            context.getString(R.string.ai_settings_auto_reply_all) + " disabled",
            Toast.LENGTH_SHORT
        ).show()
    }
}
