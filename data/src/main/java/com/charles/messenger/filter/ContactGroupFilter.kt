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
package com.charles.messenger.filter

import com.charles.messenger.extensions.removeAccents
import com.charles.messenger.model.ContactGroup
import javax.inject.Inject

class ContactGroupFilter @Inject constructor(private val contactFilter: ContactFilter) : Filter<ContactGroup>() {

    override fun filter(item: ContactGroup, query: CharSequence): Boolean {
        return item.title.removeAccents().contains(query, true) || // Name
                item.contacts.any { contact -> contactFilter.filter(contact, query) } // Contacts
    }

}
