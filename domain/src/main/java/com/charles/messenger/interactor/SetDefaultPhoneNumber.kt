/*
 * Copyright (C) 2019 Moez Bhatti <charles.bhatti@gmail.com>
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

import com.charles.messenger.repository.ContactRepository
import io.reactivex.Flowable
import javax.inject.Inject

class SetDefaultPhoneNumber @Inject constructor(
    private val contactRepo: ContactRepository
) : Interactor<SetDefaultPhoneNumber.Params>() {

    data class Params(val lookupKey: String, val phoneNumberId: Long)

    override fun buildObservable(params: Params): Flowable<*> {
        return Flowable.just(params)
                .doOnNext { (lookupKey, phoneNumberId) ->
                    contactRepo.setDefaultPhoneNumber(lookupKey, phoneNumberId)
                }
    }

}