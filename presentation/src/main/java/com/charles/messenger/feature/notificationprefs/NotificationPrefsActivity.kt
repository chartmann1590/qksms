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
package com.charles.messenger.feature.notificationprefs

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.jakewharton.rxbinding2.view.clicks
import com.charles.messenger.R
import com.charles.messenger.common.QkDialog
import com.charles.messenger.common.base.QkThemedActivity
import com.charles.messenger.common.util.extensions.animateLayoutChanges
import com.charles.messenger.common.util.extensions.setVisible
import com.charles.messenger.common.widget.PreferenceView
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import dagger.android.AndroidInjection
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class NotificationPrefsActivity : QkThemedActivity(), NotificationPrefsView {

    @Inject lateinit var previewModeDialog: QkDialog
    @Inject lateinit var actionsDialog: QkDialog
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var preferences: ViewGroup
    private lateinit var notificationsO: PreferenceView
    private lateinit var notifications: PreferenceView
    private lateinit var vibration: PreferenceView
    private lateinit var ringtone: PreferenceView
    private lateinit var previews: PreferenceView
    private lateinit var wake: PreferenceView
    private lateinit var actionsDivider: View
    private lateinit var actionsTitle: View
    private lateinit var action1: PreferenceView
    private lateinit var action2: PreferenceView
    private lateinit var action3: PreferenceView
    private lateinit var qkreplyDivider: View
    private lateinit var qkreplyTitle: View
    private lateinit var qkreply: PreferenceView
    private lateinit var qkreplyTapDismiss: PreferenceView

    override val preferenceClickIntent: Subject<PreferenceView> = PublishSubject.create()
    override val previewModeSelectedIntent by lazy { previewModeDialog.adapter.menuItemClicks }
    override val ringtoneSelectedIntent: Subject<String> = PublishSubject.create()
    override val actionsSelectedIntent by lazy { actionsDialog.adapter.menuItemClicks }

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory)[NotificationPrefsViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.notification_prefs_activity)

        preferences = findViewById(R.id.preferences)
        notificationsO = findViewById(R.id.notificationsO)
        notifications = findViewById(R.id.notifications)
        vibration = findViewById(R.id.vibration)
        ringtone = findViewById(R.id.ringtone)
        previews = findViewById(R.id.previews)
        wake = findViewById(R.id.wake)
        actionsDivider = findViewById(R.id.actionsDivider)
        actionsTitle = findViewById(R.id.actionsTitle)
        action1 = findViewById(R.id.action1)
        action2 = findViewById(R.id.action2)
        action3 = findViewById(R.id.action3)
        qkreplyDivider = findViewById(R.id.qkreplyDivider)
        qkreplyTitle = findViewById(R.id.qkreplyTitle)
        qkreply = findViewById(R.id.qkreply)
        qkreplyTapDismiss = findViewById(R.id.qkreplyTapDismiss)

        setTitle(R.string.title_notification_prefs)
        showBackButton(true)
        viewModel.bindView(this)

        preferences.postDelayed({ preferences.animateLayoutChanges = true }, 100)

        val hasOreo = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

        notificationsO.setVisible(hasOreo)
        notifications.setVisible(!hasOreo)
        vibration.setVisible(!hasOreo)
        ringtone.setVisible(!hasOreo)

        previewModeDialog.setTitle(R.string.settings_notification_previews_title)
        previewModeDialog.adapter.setData(R.array.notification_preview_options)
        actionsDialog.adapter.setData(R.array.notification_actions)

        // Listen to clicks for all of the preferences
        (0 until preferences.childCount)
                .map { index -> preferences.getChildAt(index) }
                .mapNotNull { view -> view as? PreferenceView }
                .map { preference -> preference.clicks().map { preference } }
                .let { Observable.merge(it) }
                .autoDisposable(scope())
                .subscribe(preferenceClickIntent)
    }

    override fun render(state: NotificationPrefsState) {
        if (state.threadId != 0L) {
            title = state.conversationTitle
        }

        // Handle both CheckBox and QkSwitch widgets
        val notificationsCheckbox = notifications.findViewById<View>(R.id.checkbox)
        when (notificationsCheckbox) {
            is android.widget.CheckBox -> notificationsCheckbox.isChecked = state.notificationsEnabled
            is com.charles.messenger.common.widget.QkSwitch -> notificationsCheckbox.isChecked = state.notificationsEnabled
        }
        previews.summary = state.previewSummary
        previewModeDialog.adapter.selectedItem = state.previewId
        val wakeCheckbox = wake.findViewById<View>(R.id.checkbox)
        when (wakeCheckbox) {
            is android.widget.CheckBox -> wakeCheckbox.isChecked = state.wakeEnabled
            is com.charles.messenger.common.widget.QkSwitch -> wakeCheckbox.isChecked = state.wakeEnabled
        }
        val vibrationCheckbox = vibration.findViewById<View>(R.id.checkbox)
        when (vibrationCheckbox) {
            is android.widget.CheckBox -> vibrationCheckbox.isChecked = state.vibrationEnabled
            is com.charles.messenger.common.widget.QkSwitch -> vibrationCheckbox.isChecked = state.vibrationEnabled
        }
        ringtone.summary = state.ringtoneName

        actionsDivider.isVisible = state.threadId == 0L
        actionsTitle.isVisible = state.threadId == 0L
        action1.isVisible = state.threadId == 0L
        action1.summary = state.action1Summary
        action2.isVisible = state.threadId == 0L
        action2.summary = state.action2Summary
        action3.isVisible = state.threadId == 0L
        action3.summary = state.action3Summary

        qkreplyDivider.isVisible = state.threadId == 0L
        qkreplyTitle.isVisible = state.threadId == 0L
        val qkreplyCheckbox = qkreply.findViewById<View>(R.id.checkbox)
        when (qkreplyCheckbox) {
            is android.widget.CheckBox -> qkreplyCheckbox.isChecked = state.qkReplyEnabled
            is com.charles.messenger.common.widget.QkSwitch -> qkreplyCheckbox.isChecked = state.qkReplyEnabled
        }
        qkreply.isVisible = state.threadId == 0L
        qkreplyTapDismiss.isVisible = state.threadId == 0L
        qkreplyTapDismiss.isEnabled = state.qkReplyEnabled
        val qkreplyTapDismissCheckbox = qkreplyTapDismiss.findViewById<View>(R.id.checkbox)
        when (qkreplyTapDismissCheckbox) {
            is android.widget.CheckBox -> qkreplyTapDismissCheckbox.isChecked = state.qkReplyTapDismiss
            is com.charles.messenger.common.widget.QkSwitch -> qkreplyTapDismissCheckbox.isChecked = state.qkReplyTapDismiss
        }
    }

    override fun showPreviewModeDialog() = previewModeDialog.show(this)

    override fun showRingtonePicker(default: Uri?) {
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, default)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
        startActivityForResult(intent, 123)
    }

    override fun showActionDialog(selected: Int) {
        actionsDialog.adapter.selectedItem = selected
        actionsDialog.show(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 123 && resultCode == Activity.RESULT_OK) {
            val uri: Uri? = data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            ringtoneSelectedIntent.onNext(uri?.toString() ?: "")
        }
    }

}