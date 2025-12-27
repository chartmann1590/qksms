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

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.charles.messenger.interactor.MarkFailed
import com.charles.messenger.interactor.MarkSent
import com.charles.messenger.service.WebSyncService
import com.charles.messenger.util.Preferences
import dagger.android.AndroidInjection
import timber.log.Timber
import javax.inject.Inject

class SmsSentReceiver : BroadcastReceiver() {

    @Inject lateinit var markSent: MarkSent
    @Inject lateinit var markFailed: MarkFailed
    @Inject lateinit var preferences: Preferences

    override fun onReceive(context: Context, intent: Intent) {
        AndroidInjection.inject(this, context)

        val id = intent.getLongExtra("id", 0L)

        when (resultCode) {
            Activity.RESULT_OK -> {
                val pendingResult = goAsync()
                markSent.execute(id) {
                    // Trigger instant web sync if enabled
                    if (preferences.webSyncEnabled.get()) {
                        Timber.i("SMS sent, triggering instant web sync")
                        WebSyncService.triggerInstantSync(context)
                    }
                    pendingResult.finish()
                }
            }

            else -> {
                val pendingResult = goAsync()
                markFailed.execute(MarkFailed.Params(id, resultCode)) { pendingResult.finish() }
            }
        }
    }

}
