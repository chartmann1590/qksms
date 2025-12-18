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
package com.charles.messenger.blocking

import android.content.Context
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

/**
 * Stub implementation of CallControl blocking client
 * The actual CallControl SDK dependency is not available
 */
class CallControlBlockingClient @Inject constructor(
    private val context: Context
) : BlockingClient {

    override fun isAvailable(): Boolean = false

    override fun getClientCapability() = BlockingClient.Capability.BLOCK_WITHOUT_PERMISSION

    override fun shouldBlock(address: String): Single<BlockingClient.Action> {
        return Single.just(BlockingClient.Action.Unblock)
    }

    override fun isBlacklisted(address: String): Single<BlockingClient.Action> {
        return Single.just(BlockingClient.Action.Unblock)
    }

    override fun block(addresses: List<String>): Completable {
        return Completable.complete()
    }

    override fun unblock(addresses: List<String>): Completable {
        return Completable.complete()
    }

    override fun openSettings() {
        // No-op
    }
}
