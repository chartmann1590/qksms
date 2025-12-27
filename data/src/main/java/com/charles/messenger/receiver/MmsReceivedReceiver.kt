/*
 * Copyright (C) 2017 Moez Bhatti <charles.bhatti@gmail.com>
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
package com.charles.messenger.receiver

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.klinker.android.send_message.MmsReceivedReceiver
import com.charles.messenger.interactor.ReceiveMms
import com.charles.messenger.service.WebSyncService
import com.charles.messenger.util.Preferences
import dagger.android.AndroidInjection
import timber.log.Timber
import javax.inject.Inject

class MmsReceivedReceiver : MmsReceivedReceiver() {

    @Inject lateinit var receiveMms: ReceiveMms
    @Inject lateinit var preferences: Preferences

    private var receivedContext: Context? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        AndroidInjection.inject(this, context)
        receivedContext = context
        super.onReceive(context, intent)
    }

    override fun onMessageReceived(messageUri: Uri?) {
        messageUri?.let { uri ->
            val pendingResult = goAsync()
            receiveMms.execute(uri) {
                // Trigger instant web sync if enabled
                receivedContext?.let { ctx ->
                    if (preferences.webSyncEnabled.get()) {
                        Timber.i("MMS received, triggering instant web sync")
                        WebSyncService.triggerInstantSync(ctx)
                    }
                }
                pendingResult.finish()
            }
        }
    }

}
