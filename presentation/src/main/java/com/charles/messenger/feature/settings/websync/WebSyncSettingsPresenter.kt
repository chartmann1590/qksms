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

import android.content.Context
import com.charles.messenger.common.base.QkPresenter
import com.charles.messenger.interactor.SyncToWebServer
import com.charles.messenger.interactor.TestWebConnection
import com.charles.messenger.manager.CredentialManager
import com.charles.messenger.repository.WebSyncRepository
import com.charles.messenger.service.WebSyncService
import com.charles.messenger.util.Preferences
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.rxkotlin.withLatestFrom
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class WebSyncSettingsPresenter @Inject constructor(
    private val context: Context,
    private val prefs: Preferences,
    private val credentialManager: CredentialManager,
    private val testWebConnection: TestWebConnection,
    private val syncToWebServer: SyncToWebServer
) : QkPresenter<WebSyncSettingsView, WebSyncSettingsState>(
    WebSyncSettingsState(
        enabled = false,
        serverUrl = "",
        username = "",
        password = ""
    )
) {

    override fun bindIntents(view: WebSyncSettingsView) {
        super.bindIntents(view)

        // Load initial state from preferences
        newState {
            copy(
                enabled = prefs.webSyncEnabled.get(),
                serverUrl = prefs.webSyncServerUrl.get(),
                username = prefs.webSyncUsername.get(),
                password = credentialManager.getPassword() ?: "",
                connectionStatus = if (prefs.webSyncConnectionTested.get()) ConnectionStatus.Connected else ConnectionStatus.Unknown,
                lastSyncTimestamp = prefs.webSyncLastFullSync.get()
            )
        }

        // Handle enabled toggle
        view.webSyncEnabledChanged()
            .doOnNext { enabled ->
                prefs.webSyncEnabled.set(enabled)
                Timber.d("Web Sync enabled: $enabled")

                // Schedule or cancel background sync job
                if (enabled) {
                    WebSyncService.scheduleJob(context)
                    Timber.i("Web Sync background job scheduled")
                } else {
                    WebSyncService.cancelJob(context)
                    Timber.i("Web Sync background job canceled")
                }
            }
            .autoDisposable(view.scope())
            .subscribe { enabled ->
                newState { copy(enabled = enabled) }
                if (enabled) {
                    view.showToast("Web Sync enabled. Automatic sync scheduled every 30 minutes.")
                } else {
                    view.showToast("Web Sync disabled. Background sync canceled.")
                }
            }

        // Handle server URL changes
        view.serverUrlChanged()
            .doOnNext { url ->
                prefs.webSyncServerUrl.set(url)
                prefs.webSyncConnectionTested.set(false)
                Timber.d("Server URL updated: $url")
            }
            .autoDisposable(view.scope())
            .subscribe { url ->
                newState { copy(serverUrl = url, connectionStatus = ConnectionStatus.Unknown) }
            }

        // Handle username changes
        view.usernameChanged()
            .doOnNext { username ->
                prefs.webSyncUsername.set(username)
                prefs.webSyncConnectionTested.set(false)
                Timber.d("Username updated: $username")
            }
            .autoDisposable(view.scope())
            .subscribe { username ->
                newState { copy(username = username, connectionStatus = ConnectionStatus.Unknown) }
            }

        // Handle password changes
        view.passwordChanged()
            .doOnNext { password ->
                credentialManager.savePassword(password)
                prefs.webSyncConnectionTested.set(false)
                Timber.d("Password updated")
            }
            .autoDisposable(view.scope())
            .subscribe { password ->
                newState { copy(password = password, connectionStatus = ConnectionStatus.Unknown) }
            }

        // Handle test connection
        view.testConnectionClicks()
            .withLatestFrom(state) { _, state -> state }
            .doOnNext { state ->
                Timber.d("Testing connection to ${state.serverUrl}")
                newState { copy(connectionStatus = ConnectionStatus.Testing) }
            }
            .switchMap { state ->
                testWebConnection.buildObservable(
                    TestWebConnection.Params(
                        serverUrl = state.serverUrl,
                        username = state.username,
                        password = state.password
                    )
                ).toObservable() as Observable<Boolean>
            }
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(view.scope())
            .subscribe(
                { success ->
                    Timber.i("Connection test result: $success")
                    prefs.webSyncConnectionTested.set(success)
                    newState {
                        copy(connectionStatus = if (success) ConnectionStatus.Connected else ConnectionStatus.Failed)
                    }
                    view.showToast(
                        if (success) "Connection successful!"
                        else "Connection failed. Check your settings."
                    )
                },
                { error: Throwable ->
                    Timber.e(error, "Connection test error")
                    prefs.webSyncConnectionTested.set(false)
                    newState { copy(connectionStatus = ConnectionStatus.Failed) }
                    view.showToast("Connection error: ${error.message}")
                }
            )

        // Handle full sync
        view.performFullSyncClicks()
            .withLatestFrom(state) { _, state -> state }
            .doOnNext { state ->
                if (state.serverUrl.isEmpty() || state.username.isEmpty() || state.password.isEmpty()) {
                    view.showToast("Please configure server settings first")
                    return@doOnNext
                }
                if (state.connectionStatus != ConnectionStatus.Connected) {
                    view.showToast("Please test connection first")
                    return@doOnNext
                }
                Timber.d("Starting full sync")
                newState { copy(syncInProgress = true, syncProgress = "Starting sync...") }
            }
            .filter { state ->
                state.serverUrl.isNotEmpty() &&
                state.username.isNotEmpty() &&
                state.password.isNotEmpty() &&
                state.connectionStatus == ConnectionStatus.Connected
            }
            .switchMap {
                syncToWebServer.buildObservable(SyncToWebServer.Params(isFullSync = true))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .toObservable()
            }
            .autoDisposable(view.scope())
            .subscribe(
                { progress ->
                    val message = when (progress.stage) {
                        WebSyncRepository.SyncProgress.Stage.AUTHENTICATING -> "Authenticating..."
                        WebSyncRepository.SyncProgress.Stage.SYNCING_CONVERSATIONS ->
                            "Syncing conversations..."
                        WebSyncRepository.SyncProgress.Stage.SYNCING_MESSAGES ->
                            "Syncing messages: ${progress.current}/${progress.total}"
                        WebSyncRepository.SyncProgress.Stage.UPLOADING_ATTACHMENTS ->
                            "Uploading attachments: ${progress.current}/${progress.total}"
                        WebSyncRepository.SyncProgress.Stage.COMPLETE -> "Sync complete!"
                        WebSyncRepository.SyncProgress.Stage.ERROR -> "Sync failed"
                    }

                    Timber.v("Sync progress: $message")
                    newState { copy(syncProgress = message) }

                    if (progress.stage == WebSyncRepository.SyncProgress.Stage.COMPLETE) {
                        prefs.webSyncLastFullSync.set(System.currentTimeMillis())
                        newState {
                            copy(
                                syncInProgress = false,
                                lastSyncTimestamp = System.currentTimeMillis()
                            )
                        }

                        // Ensure background sync is scheduled after successful full sync
                        if (prefs.webSyncEnabled.get()) {
                            WebSyncService.scheduleJob(context)
                            Timber.i("Web Sync background job scheduled after full sync")
                        }

                        view.showToast("Full sync completed successfully!")
                    } else if (progress.stage == WebSyncRepository.SyncProgress.Stage.ERROR) {
                        newState { copy(syncInProgress = false) }
                        view.showToast("Sync failed: ${progress.message}")
                    }
                },
                { error: Throwable ->
                    Timber.e(error, "Full sync error")
                    newState { copy(syncInProgress = false, syncProgress = "" ) }
                    view.showToast("Sync error: ${error.message}")
                }
            )
    }
}
