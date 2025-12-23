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
import android.widget.ImageView
import com.charles.messenger.R
import com.charles.messenger.common.base.QkAdapter
import com.charles.messenger.common.base.QkViewHolder
import com.charles.messenger.util.GlideApp
import javax.inject.Inject

class ScheduledMessageAttachmentAdapter @Inject constructor(
    private val context: Context
) : QkAdapter<Uri>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.scheduled_message_image_list_item, parent, false)
        val thumbnail = view.findViewById<ImageView>(R.id.thumbnail)
        thumbnail.clipToOutline = true

        return QkViewHolder(view)
    }

    override fun onBindViewHolder(holder: QkViewHolder, position: Int) {
        val attachment = getItem(position)
        val thumbnail = holder.itemView.findViewById<ImageView>(R.id.thumbnail)

        GlideApp.with(context).load(attachment).into(thumbnail)
    }

}
