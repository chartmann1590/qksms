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
package com.charles.messenger.interactor

import android.net.Uri
import com.charles.messenger.extensions.mapNotNull
import com.charles.messenger.repository.ConversationRepository
import com.charles.messenger.repository.SyncRepository
import io.reactivex.Flowable
import javax.inject.Inject

class SyncMessage @Inject constructor(
    private val conversationRepo: ConversationRepository,
    private val syncManager: SyncRepository,
    private val updateBadge: UpdateBadge
) : Interactor<Uri>() {

    override fun buildObservable(params: Uri): Flowable<*> {
        return Flowable.just(params)
                .mapNotNull { uri -> syncManager.syncMessage(uri) }
                .doOnNext { message -> conversationRepo.updateConversations(message.threadId) }
                .flatMap { updateBadge.buildObservable(Unit) } // Update the badge
    }

}