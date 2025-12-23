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
package com.charles.messenger.feature.compose.editing

import android.content.Context
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.charles.messenger.R
import com.charles.messenger.common.util.Colors
import com.charles.messenger.common.util.extensions.setBackgroundTint
import com.charles.messenger.common.util.extensions.setTint
import com.charles.messenger.common.widget.AvatarView
import com.charles.messenger.injection.appComponent
import com.charles.messenger.model.Recipient
import javax.inject.Inject

class DetailedChipView(context: Context) : RelativeLayout(context) {

    @Inject lateinit var colors: Colors

    private lateinit var avatar: AvatarView
    private lateinit var name: TextView
    private lateinit var info: TextView
    private lateinit var card: CardView
    private lateinit var delete: ImageView

    init {
        View.inflate(context, R.layout.contact_chip_detailed, this)
        appComponent.inject(this)

        avatar = findViewById(R.id.avatar)
        name = findViewById(R.id.name)
        info = findViewById(R.id.info)
        card = findViewById(R.id.card)
        delete = findViewById(R.id.delete)

        setOnClickListener { hide() }

        visibility = View.GONE

        isFocusable = true
        isFocusableInTouchMode = true
    }

    fun setRecipient(recipient: Recipient) {
        avatar.setRecipient(recipient)
        name.text = recipient.contact?.name?.takeIf { it.isNotBlank() } ?: recipient.address
        info.text = recipient.address

        colors.theme(recipient).let { theme ->
            card.setBackgroundTint(theme.theme)
            name.setTextColor(theme.textPrimary)
            info.setTextColor(theme.textTertiary)
            delete.setTint(theme.textPrimary)
        }
    }

    fun show() {
        startAnimation(AlphaAnimation(0f, 1f).apply { duration = 200 })

        visibility = View.VISIBLE
        requestFocus()
        isClickable = true
    }

    fun hide() {
        startAnimation(AlphaAnimation(1f, 0f).apply { duration = 200 })

        visibility = View.GONE
        clearFocus()
        isClickable = false
    }

    fun setOnDeleteListener(listener: (View) -> Unit) {
        delete.setOnClickListener(listener)
    }

}
