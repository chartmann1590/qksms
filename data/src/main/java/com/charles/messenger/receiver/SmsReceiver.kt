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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony.Sms
import com.charles.messenger.interactor.ReceiveSms
import com.charles.messenger.service.WebSyncService
import com.charles.messenger.util.Preferences
import dagger.android.AndroidInjection
import timber.log.Timber
import javax.inject.Inject

class SmsReceiver : BroadcastReceiver() {

    @Inject lateinit var receiveMessage: ReceiveSms
    @Inject lateinit var preferences: Preferences

    override fun onReceive(context: Context, intent: Intent) {
        AndroidInjection.inject(this, context)
        Timber.v("onReceive")

        Sms.Intents.getMessagesFromIntent(intent)?.let { messages ->
            val subId = intent.extras?.getInt("subscription", -1) ?: -1

            val pendingResult = goAsync()
            receiveMessage.execute(ReceiveSms.Params(subId, messages)) {
                // Trigger instant web sync if enabled
                if (preferences.webSyncEnabled.get()) {
                    Timber.i("SMS received, triggering instant web sync")
                    WebSyncService.triggerInstantSync(context)
                }
                pendingResult.finish()
            }
        }
    }

}