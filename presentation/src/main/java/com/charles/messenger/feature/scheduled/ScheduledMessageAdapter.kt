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
package com.charles.messenger.feature.scheduled

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.charles.messenger.R
import com.charles.messenger.common.base.QkRealmAdapter
import com.charles.messenger.common.base.QkViewHolder
import com.charles.messenger.common.util.DateFormatter
import com.charles.messenger.common.widget.GroupAvatarView
import com.charles.messenger.common.widget.QkTextView
import com.charles.messenger.model.Contact
import com.charles.messenger.model.Recipient
import com.charles.messenger.model.ScheduledMessage
import com.charles.messenger.repository.ContactRepository
import com.charles.messenger.util.PhoneNumberUtils
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class ScheduledMessageAdapter @Inject constructor(
    private val context: Context,
    private val contactRepo: ContactRepository,
    private val dateFormatter: DateFormatter,
    private val phoneNumberUtils: PhoneNumberUtils
) : QkRealmAdapter<ScheduledMessage>() {

    private val contacts by lazy { contactRepo.getContacts() }
    private val contactCache = ContactCache()
    private val imagesViewPool = RecyclerView.RecycledViewPool()

    val clicks: Subject<Long> = PublishSubject.create()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.scheduled_message_list_item, parent, false)

        val attachments = view.findViewById<RecyclerView>(R.id.attachments)
        attachments.adapter = ScheduledMessageAttachmentAdapter(context)
        attachments.setRecycledViewPool(imagesViewPool)

        return QkViewHolder(view).apply {
            view.setOnClickListener {
                val message = getItem(adapterPosition) ?: return@setOnClickListener
                clicks.onNext(message.id)
            }
        }
    }

    override fun onBindViewHolder(holder: QkViewHolder, position: Int) {
        val message = getItem(position) ?: return

        val avatars = holder.itemView.findViewById<GroupAvatarView>(R.id.avatars)
        val recipients = holder.itemView.findViewById<QkTextView>(R.id.recipients)
        val date = holder.itemView.findViewById<QkTextView>(R.id.date)
        val body = holder.itemView.findViewById<QkTextView>(R.id.body)
        val attachments = holder.itemView.findViewById<RecyclerView>(R.id.attachments)

        // GroupAvatarView only accepts recipients, so map the phone numbers to recipients
        avatars.recipients = message.recipients.map { address -> Recipient(address = address) }

        recipients.text = message.recipients.joinToString(",") { address ->
            contactCache[address]?.name?.takeIf { it.isNotBlank() } ?: address
        }

        date.text = dateFormatter.getScheduledTimestamp(message.date)
        body.text = message.body

        val adapter = attachments.adapter as ScheduledMessageAttachmentAdapter
        adapter.data = message.attachments.map(Uri::parse)
        attachments.isVisible = message.attachments.isNotEmpty()
    }

    /**
     * Cache the contacts in a map by the address, because the messages we're binding don't have
     * a reference to the contact.
     */
    private inner class ContactCache : HashMap<String, Contact?>() {

        override fun get(key: String): Contact? {
            if (super.get(key)?.isValid != true) {
                set(key, contacts.firstOrNull { contact ->
                    contact.numbers.any {
                        phoneNumberUtils.compare(it.address, key)
                    }
                })
            }

            return super.get(key)?.takeIf { it.isValid }
        }

    }

}
