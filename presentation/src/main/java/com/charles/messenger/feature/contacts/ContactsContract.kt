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
package com.charles.messenger.feature.contacts

import com.charles.messenger.common.base.QkView
import com.charles.messenger.extensions.Optional
import com.charles.messenger.feature.compose.editing.ComposeItem
import com.charles.messenger.feature.compose.editing.PhoneNumberAction
import io.reactivex.Observable
import io.reactivex.subjects.Subject

interface ContactsContract : QkView<ContactsState> {

    val queryChangedIntent: Observable<CharSequence>
    val queryClearedIntent: Observable<*>
    val queryEditorActionIntent: Observable<Int>
    val composeItemPressedIntent: Subject<ComposeItem>
    val composeItemLongPressedIntent: Subject<ComposeItem>
    val phoneNumberSelectedIntent: Subject<Optional<Long>>
    val phoneNumberActionIntent: Subject<PhoneNumberAction>

    fun clearQuery()
    fun openKeyboard()
    fun finish(result: HashMap<String, String?>)

}
