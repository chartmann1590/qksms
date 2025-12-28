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
package com.charles.messenger.service

import android.annotation.SuppressLint
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import com.charles.messenger.common.util.extensions.jobScheduler
import com.charles.messenger.compat.TelephonyCompat
import com.charles.messenger.interactor.ConfirmMessageSent
import com.charles.messenger.interactor.FetchQueuedWebMessages
import com.charles.messenger.interactor.SendMessage
import com.charles.messenger.interactor.SyncToWebServer
import com.charles.messenger.repository.MessageRepository
import com.charles.messenger.util.Preferences
import dagger.android.AndroidInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Background service that performs periodic incremental syncs to the web server
 * when web sync is enabled. Runs every 1 minute for near-instant sync.
 */
class WebSyncService : JobService() {

    companion object {
        private const val JobId = 8120236
        private const val InstantJobId = 8120237

        /**
         * Schedule periodic incremental sync job (every 1 minute for near-instant sync)
         */
        @SuppressLint("MissingPermission") // Added in [presentation]'s AndroidManifest.xml
        fun scheduleJob(context: Context) {
            Timber.i("Scheduling web sync job (every 1 minute)")
            val serviceComponent = ComponentName(context, WebSyncService::class.java)
            val periodicJob = JobInfo.Builder(JobId, serviceComponent)
                .setPeriodic(TimeUnit.MINUTES.toMillis(1)) // Changed from 30 to 1 minute
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY) // Requires network connection
                .setPersisted(true) // Persist across device reboots
                .build()

            context.jobScheduler.schedule(periodicJob)
        }

        /**
         * Trigger instant sync immediately (for when messages are sent/received)
         */
        @SuppressLint("MissingPermission")
        fun triggerInstantSync(context: Context) {
            Timber.i("Triggering instant web sync")
            val serviceComponent = ComponentName(context, WebSyncService::class.java)
            val instantJob = JobInfo.Builder(InstantJobId, serviceComponent)
                .setOverrideDeadline(0) // Run immediately
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .build()

            context.jobScheduler.schedule(instantJob)
        }

        /**
         * Cancel the periodic sync job
         */
        fun cancelJob(context: Context) {
            Timber.i("Canceling web sync job")
            context.jobScheduler.cancel(JobId)
            context.jobScheduler.cancel(InstantJobId)
        }
    }

    @Inject lateinit var syncToWebServer: SyncToWebServer
    @Inject lateinit var fetchQueuedWebMessages: FetchQueuedWebMessages
    @Inject lateinit var sendMessage: SendMessage
    @Inject lateinit var confirmMessageSent: ConfirmMessageSent
    @Inject lateinit var messageRepository: MessageRepository
    @Inject lateinit var preferences: Preferences

    private val disposables = CompositeDisposable()

    override fun onStartJob(params: JobParameters?): Boolean {
        Timber.i("WebSyncService: onStartJob")
        AndroidInjection.inject(this)

        // Only sync if web sync is enabled
        if (!preferences.webSyncEnabled.get()) {
            Timber.i("WebSyncService: Web sync disabled, skipping")
            jobFinished(params, false)
            return false
        }

        // Perform incremental sync, then fetch and send queued messages
        disposables += syncToWebServer.buildObservable(SyncToWebServer.Params(isFullSync = false))
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .ignoreElements()
            .doOnComplete {
                Timber.i("WebSyncService: Incremental sync completed")
            }
            .andThen(fetchQueuedWebMessages.buildObservable(Unit).firstOrError())
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe(
                { queuedMessages: Any ->
                    if (queuedMessages is List<*>) {
                        @Suppress("UNCHECKED_CAST")
                        val messages = queuedMessages as List<com.charles.messenger.repository.WebSyncRepository.QueuedMessage>

                        if (messages.isEmpty()) {
                            Timber.i("WebSyncService: No queued messages to send")
                            jobFinished(params, false)
                            return@subscribe
                        }

                        Timber.i("WebSyncService: Found ${messages.size} queued messages to send")

                        // Send each queued message
                        messages.forEach { queuedMsg ->
                            sendQueuedMessage(queuedMsg, params)
                        }

                        // Job finished after processing all messages
                        jobFinished(params, false)
                    }
                },
                { error: Throwable ->
                    Timber.e(error, "WebSyncService: Error fetching queued messages")
                    jobFinished(params, false)
                }
            )

        return true // Job is running asynchronously
    }

    private fun sendQueuedMessage(
        queuedMsg: com.charles.messenger.repository.WebSyncRepository.QueuedMessage,
        params: JobParameters?
    ) {
        try {
            Timber.i("WebSyncService: Sending queued message to ${queuedMsg.addresses}")

            // Get or create threadId
            val threadId = queuedMsg.conversationId
                ?: TelephonyCompat.getOrCreateThreadId(this, queuedMsg.addresses.toSet())

            // Get default subscription ID (subId -1 means default)
            val subId = -1

            // Send the message
            disposables += sendMessage.buildObservable(
                SendMessage.Params(
                    subId = subId,
                    threadId = threadId,
                    addresses = queuedMsg.addresses,
                    body = queuedMsg.body,
                    attachments = emptyList()
                )
            )
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe(
                { result ->
                    // Message sent successfully, find the message ID and confirm
                    val messages = messageRepository.getMessages(threadId)
                    val lastMessage = messages.maxByOrNull { it.date }

                    if (lastMessage != null) {
                        Timber.i("WebSyncService: Message sent successfully, confirming with server (messageId: ${lastMessage.id})")

                        // Confirm with server
                        disposables += confirmMessageSent.buildObservable(
                            ConfirmMessageSent.Params(
                                queueId = queuedMsg.queueId,
                                androidMessageId = lastMessage.id
                            )
                        )
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                            { confirmed ->
                                Timber.i("WebSyncService: Message confirmation sent: $confirmed")
                            },
                            { error ->
                                Timber.e(error, "WebSyncService: Error confirming message sent")
                            }
                        )
                    } else {
                        Timber.w("WebSyncService: Could not find sent message to confirm")
                    }
                },
                { error ->
                    Timber.e(error, "WebSyncService: Error sending queued message")
                }
            )
        } catch (e: Exception) {
            Timber.e(e, "WebSyncService: Exception sending queued message")
        }
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        Timber.i("WebSyncService: onStopJob")
        disposables.dispose()
        return true // Reschedule if job was interrupted
    }
}
