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
package com.charles.messenger.repository

import io.reactivex.Flowable
import io.reactivex.Single

interface WebSyncRepository {

    /**
     * Test connection to web server with credentials
     * @return Single<Boolean> true if connection successful
     */
    fun testConnection(serverUrl: String, username: String, password: String): Single<Boolean>

    /**
     * Perform full sync of all messages to web server
     * @return Flowable<SyncProgress> stream of sync progress updates
     */
    fun performFullSync(): Flowable<SyncProgress>

    /**
     * Perform incremental sync of new/updated messages
     * @return Flowable<SyncProgress> stream of sync progress updates
     */
    fun performIncrementalSync(): Flowable<SyncProgress>

    /**
     * Fetch messages queued from web interface to be sent
     * @return Single<List<QueuedMessage>> list of messages to send
     */
    fun fetchQueuedMessages(): Single<List<QueuedMessage>>

    /**
     * Confirm that a queued message was sent successfully
     * @param queueId ID of queued message from server
     * @param androidMessageId ID of message in Android SMS provider
     * @return Single<Boolean> true if confirmation successful
     */
    fun confirmMessageSent(queueId: String, androidMessageId: Long): Single<Boolean>

    data class SyncProgress(
        val stage: Stage,
        val current: Int,
        val total: Int,
        val message: String = ""
    ) {
        enum class Stage {
            AUTHENTICATING,
            SYNCING_CONVERSATIONS,
            SYNCING_MESSAGES,
            UPLOADING_ATTACHMENTS,
            COMPLETE,
            ERROR
        }

        val percentage: Int
            get() = if (total > 0) (current * 100 / total) else 0
    }

    data class QueuedMessage(
        val queueId: String,
        val conversationId: Long?,
        val addresses: List<String>,
        val body: String
    )
}
