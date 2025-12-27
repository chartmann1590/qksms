/*
 * Copyright (C) 2024 Charles Hartmann
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.charles.messenger.feature.settings.websync

import android.app.AlertDialog
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.checkedChanges
import com.charles.messenger.R
import com.charles.messenger.common.base.QkController
import com.charles.messenger.common.widget.PreferenceView
import com.charles.messenger.common.widget.QkSwitch
import com.charles.messenger.injection.appComponent
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class WebSyncSettingsController : QkController<WebSyncSettingsView, WebSyncSettingsState, WebSyncSettingsPresenter>(), WebSyncSettingsView {

    @Inject override lateinit var presenter: WebSyncSettingsPresenter

    private val serverUrlChangedSubject: Subject<String> = PublishSubject.create()
    private val usernameChangedSubject: Subject<String> = PublishSubject.create()
    private val passwordChangedSubject: Subject<String> = PublishSubject.create()

    private lateinit var preferences: LinearLayout
    private lateinit var webSyncEnabled: PreferenceView
    private lateinit var serverUrl: PreferenceView
    private lateinit var username: PreferenceView
    private lateinit var password: PreferenceView
    private lateinit var testConnection: Button
    private lateinit var connectionStatus: TextView
    private lateinit var performFullSync: Button
    private lateinit var syncStatus: TextView
    private lateinit var syncProgressBar: android.widget.ProgressBar

    init {
        appComponent.inject(this)
        layoutRes = R.layout.web_sync_settings_controller
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        preferences = view.findViewById(R.id.preferences)
        webSyncEnabled = view.findViewById(R.id.webSyncEnabled)
        serverUrl = view.findViewById(R.id.serverUrl)
        username = view.findViewById(R.id.username)
        password = view.findViewById(R.id.password)
        testConnection = view.findViewById(R.id.testConnection)
        connectionStatus = view.findViewById(R.id.connectionStatus)
        performFullSync = view.findViewById(R.id.performFullSync)
        syncStatus = view.findViewById(R.id.syncStatus)
        syncProgressBar = view.findViewById(R.id.syncProgressBar)

        // Bind intents AFTER all views are initialized
        presenter.bindIntents(this)
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        setTitle(R.string.web_sync_title)
        showBackButton(true)

        // Handle clicks on enabled switch
        webSyncEnabled.clicks().subscribe {
            val switch = webSyncEnabled.widget?.findViewById<QkSwitch>(R.id.checkbox)
            switch?.isChecked = !(switch?.isChecked ?: false)
        }
    }

    override fun webSyncEnabledChanged(): Observable<Boolean> {
        return webSyncEnabled.widget?.let { widget ->
            (widget.findViewById<View>(R.id.checkbox) as? QkSwitch)
                ?.checkedChanges()
                ?.skipInitialValue()
        } ?: Observable.empty()
    }

    override fun serverUrlChanged(): Observable<String> = serverUrlChangedSubject

    override fun usernameChanged(): Observable<String> = usernameChangedSubject

    override fun passwordChanged(): Observable<String> = passwordChangedSubject

    override fun testConnectionClicks(): Observable<Unit> = testConnection.clicks()

    override fun performFullSyncClicks(): Observable<Unit> = performFullSync.clicks()

    override fun showServerUrlDialog(currentUrl: String) {
        val editText = EditText(activity!!).apply {
            setText(currentUrl)
            hint = "http://192.168.1.100:8080"
            inputType = InputType.TYPE_TEXT_VARIATION_URI
        }

        AlertDialog.Builder(activity)
            .setTitle(R.string.web_sync_server_url)
            .setView(editText)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val url = editText.text.toString().trim()
                if (url.isNotEmpty()) {
                    serverUrlChangedSubject.onNext(url)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun showUsernameDialog(currentUsername: String) {
        val editText = EditText(activity!!).apply {
            setText(currentUsername)
            hint = activity!!.getString(R.string.web_sync_username_hint)
            inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        }

        AlertDialog.Builder(activity)
            .setTitle(R.string.web_sync_username)
            .setView(editText)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val user = editText.text.toString().trim()
                if (user.isNotEmpty()) {
                    usernameChangedSubject.onNext(user)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun showPasswordDialog() {
        val editText = EditText(activity!!).apply {
            hint = activity!!.getString(R.string.web_sync_password_hint)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        AlertDialog.Builder(activity)
            .setTitle(R.string.web_sync_password)
            .setMessage(R.string.web_sync_password_warning)
            .setView(editText)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val pass = editText.text.toString()
                if (pass.length >= 12) {
                    passwordChangedSubject.onNext(pass)
                } else {
                    showToast(activity!!.getString(R.string.web_sync_password_too_short))
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun showToast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    override fun render(state: WebSyncSettingsState) {
        // Update enabled switch
        webSyncEnabled.widget?.let { widget ->
            (widget.findViewById<View>(R.id.checkbox) as? QkSwitch)?.isChecked = state.enabled
        }

        // Update server URL summary
        serverUrl.summary = state.serverUrl.ifEmpty { activity!!.getString(R.string.web_sync_server_url_not_configured) }

        // Handle server URL click
        serverUrl.clicks().subscribe {
            showServerUrlDialog(state.serverUrl)
        }

        // Update username summary
        username.summary = state.username.ifEmpty { activity!!.getString(R.string.web_sync_username_not_configured) }

        // Handle username click
        username.clicks().subscribe {
            showUsernameDialog(state.username)
        }

        // Update password summary
        password.summary = if (state.password.isNotEmpty()) {
            "••••••••"
        } else {
            activity!!.getString(R.string.web_sync_password_not_configured)
        }

        // Handle password click
        password.clicks().subscribe {
            showPasswordDialog()
        }

        // Update connection status
        connectionStatus.text = when (state.connectionStatus) {
            ConnectionStatus.Unknown -> ""
            ConnectionStatus.Testing -> activity!!.getString(R.string.web_sync_testing_connection)
            ConnectionStatus.Connected -> activity!!.getString(R.string.web_sync_connected)
            ConnectionStatus.Failed -> activity!!.getString(R.string.web_sync_connection_failed)
        }

        connectionStatus.visibility = if (state.connectionStatus == ConnectionStatus.Unknown) {
            View.GONE
        } else {
            View.VISIBLE
        }

        // Update sync status
        syncStatus.text = when {
            state.syncInProgress -> state.syncProgress
            state.lastSyncTimestamp > 0 -> {
                val formatter = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
                activity!!.getString(R.string.web_sync_last_synced, formatter.format(Date(state.lastSyncTimestamp)))
            }
            else -> activity!!.getString(R.string.web_sync_not_synced)
        }
        // Show/hide sync progress bar        syncProgressBar.visibility = if (state.syncInProgress) View.VISIBLE else View.GONE

        // Enable/disable buttons based on state
        testConnection.isEnabled = state.serverUrl.isNotEmpty() &&
            state.username.isNotEmpty() &&
            state.password.isNotEmpty() &&
            !state.syncInProgress

        performFullSync.isEnabled = state.enabled &&
            state.connectionStatus == ConnectionStatus.Connected &&
            !state.syncInProgress
    }
}
