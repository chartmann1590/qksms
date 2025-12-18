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
import com.charles.messenger.interactor.GenerateSmartReplies
import com.charles.messenger.interactor.SendMessage
import com.charles.messenger.repository.ConversationRepository
import com.charles.messenger.repository.MessageRepository
import com.charles.messenger.util.Preferences
import dagger.android.AndroidInjection
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import timber.log.Timber
import javax.inject.Inject

class AiAutoReplyReceiver : BroadcastReceiver() {

    @Inject lateinit var prefs: Preferences
    @Inject lateinit var generateSmartReplies: GenerateSmartReplies
    @Inject lateinit var sendMessage: SendMessage
    @Inject lateinit var messageRepo: MessageRepository
    @Inject lateinit var conversationRepo: ConversationRepository

    private val disposables = CompositeDisposable()

    override fun onReceive(context: Context, intent: Intent) {
        try {
            AndroidInjection.inject(this, context)
        } catch (e: Exception) {
            Timber.e(e, "Failed to inject dependencies")
            return
        }

        // Check if auto-reply is enabled
        if (!prefs.aiAutoReplyToAll.get()) {
            Timber.d("Auto-reply disabled, skipping")
            return
        }

        // Check if AI is configured
        if (!prefs.aiReplyEnabled.get() || prefs.ollamaModel.get().isEmpty()) {
            Timber.w("AI not configured, cannot auto-reply")
            return
        }

        // Get thread ID from intent extras (this varies by device)
        val threadId = intent.getLongExtra("thread_id", -1L)
        if (threadId == -1L) {
            Timber.w("Could not determine thread ID, skipping auto-reply")
            return
        }

        Timber.d("Auto-reply triggered for thread: $threadId")

        // Get conversation
        val conversation = conversationRepo.getConversation(threadId)
        if (conversation == null) {
            Timber.w("Conversation not found for thread: $threadId")
            return
        }

        // Get recent messages for context
        val messages = messageRepo.getMessages(threadId).takeLast(10)
        if (messages.isEmpty()) {
            Timber.w("No messages found for thread: $threadId")
            return
        }

        // Generate and send reply
        disposables += generateSmartReplies
            .buildObservable(GenerateSmartReplies.Params(
                baseUrl = prefs.ollamaApiUrl.get(),
                model = prefs.ollamaModel.get(),
                messages = messages
            ))
            .firstOrError()
            .flatMap { suggestions ->
                if (suggestions.isEmpty()) {
                    Timber.w("No suggestions generated")
                    throw Exception("No suggestions available")
                }

                val replyText = suggestions.first()
                Timber.d("Auto-replying with: $replyText")

                // Send the first suggestion as reply
                sendMessage.buildObservable(SendMessage.Params(
                    subId = -1,
                    threadId = threadId,
                    addresses = listOf(conversation.recipients.first().address),
                    body = replyText,
                    attachments = emptyList()
                )).firstOrError()
            }
            .subscribe(
                {
                    Timber.d("Auto-reply sent successfully")
                    disposables.clear()
                },
                { error ->
                    Timber.e(error, "Auto-reply failed")
                    disposables.clear()
                }
            )
    }
}
