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

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jakewharton.rxbinding2.view.clicks
import com.charles.messenger.R
import com.charles.messenger.common.QkDialog
import com.charles.messenger.common.base.QkThemedActivity
import com.charles.messenger.common.util.FontProvider
import com.charles.messenger.common.util.extensions.setBackgroundTint
import com.charles.messenger.common.util.extensions.setTint
import dagger.android.AndroidInjection
import javax.inject.Inject


class ScheduledActivity : QkThemedActivity(), ScheduledView {

    @Inject lateinit var dialog: QkDialog
    @Inject lateinit var fontProvider: FontProvider
    @Inject lateinit var messageAdapter: ScheduledMessageAdapter
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var collapsingToolbar: CollapsingToolbarLayout
    private lateinit var empty: View
    private lateinit var messages: RecyclerView
    private lateinit var sampleMessage: TextView
    private lateinit var compose: FloatingActionButton
    private lateinit var upgrade: View
    private lateinit var upgradeIcon: ImageView
    private lateinit var upgradeLabel: TextView

    override val messageClickIntent by lazy { messageAdapter.clicks }
    override val messageMenuIntent by lazy { dialog.adapter.menuItemClicks }
    override val composeIntent by lazy { compose.clicks() }
    override val upgradeIntent by lazy { upgrade.clicks() }

    private val viewModel by lazy { ViewModelProviders.of(this, viewModelFactory)[ScheduledViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.scheduled_activity)

        collapsingToolbar = findViewById(R.id.collapsingToolbar)
        empty = findViewById(R.id.empty)
        messages = findViewById(R.id.messages)
        sampleMessage = findViewById(R.id.sampleMessage)
        compose = findViewById(R.id.compose)
        upgrade = findViewById(R.id.upgrade)
        upgradeIcon = findViewById(R.id.upgradeIcon)
        upgradeLabel = findViewById(R.id.upgradeLabel)

        setTitle(R.string.scheduled_title)
        showBackButton(true)
        viewModel.bindView(this)

        if (!prefs.systemFont.get()) {
            fontProvider.getLato { lato ->
                val typeface = Typeface.create(lato, Typeface.BOLD)
                collapsingToolbar.setCollapsedTitleTypeface(typeface)
                collapsingToolbar.setExpandedTitleTypeface(typeface)
            }
        }

        dialog.title = getString(R.string.scheduled_options_title)
        dialog.adapter.setData(R.array.scheduled_options)

        messageAdapter.emptyView = empty
        messages.adapter = messageAdapter

        colors.theme().let { theme ->
            sampleMessage.setBackgroundTint(theme.theme)
            sampleMessage.setTextColor(theme.textPrimary)
            compose.setTint(theme.textPrimary)
            compose.setBackgroundTint(theme.theme)
            upgrade.setBackgroundTint(theme.theme)
            upgradeIcon.setTint(theme.textPrimary)
            upgradeLabel.setTextColor(theme.textPrimary)
        }
    }

    override fun render(state: ScheduledState) {
        messageAdapter.updateData(state.scheduledMessages)

        compose.isVisible = state.upgraded
        upgrade.isVisible = !state.upgraded
    }

    override fun showMessageOptions() {
        dialog.show(this)
    }

}