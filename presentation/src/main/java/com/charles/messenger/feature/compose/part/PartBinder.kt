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
package com.charles.messenger.feature.compose.part

import com.charles.messenger.common.base.QkViewHolder
import com.charles.messenger.common.util.Colors
import com.charles.messenger.model.Message
import com.charles.messenger.model.MmsPart
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject

abstract class PartBinder {

    val clicks: Subject<Long> = PublishSubject.create()

    abstract val partLayout: Int

    abstract var theme: Colors.Theme

    abstract fun canBindPart(part: MmsPart): Boolean

    abstract fun bindPart(
        holder: QkViewHolder,
        part: MmsPart,
        message: Message,
        canGroupWithPrevious: Boolean,
        canGroupWithNext: Boolean
    )

}
