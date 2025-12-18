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
package com.charles.messenger.feature.settings.ai

import com.charles.messenger.common.base.QkPresenter
import com.charles.messenger.interactor.FetchOllamaModels
import com.charles.messenger.util.Preferences
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import io.reactivex.rxkotlin.withLatestFrom
import timber.log.Timber
import javax.inject.Inject

class AiSettingsPresenter @Inject constructor(
    private val prefs: Preferences,
    private val fetchOllamaModels: FetchOllamaModels
) : QkPresenter<AiSettingsView, AiSettingsState>(
    AiSettingsState(
        aiEnabled = false,
        ollamaUrl = "",
        selectedModel = "",
        autoReplyToAll = false
    )
) {

    override fun bindIntents(view: AiSettingsView) {
        super.bindIntents(view)

        // Load initial state from preferences
        newState {
            copy(
                aiEnabled = prefs.aiReplyEnabled.get(),
                ollamaUrl = prefs.ollamaApiUrl.get(),
                selectedModel = prefs.ollamaModel.get(),
                autoReplyToAll = prefs.aiAutoReplyToAll.get()
            )
        }

        // Handle AI enabled toggle
        view.aiEnabledChanged()
            .doOnNext { enabled ->
                prefs.aiReplyEnabled.set(enabled)
                Timber.d("AI Reply enabled: $enabled")
            }
            .autoDisposable(view.scope())
            .subscribe { enabled ->
                newState { copy(aiEnabled = enabled) }
            }

        // Handle URL changes
        view.ollamaUrlChanged()
            .doOnNext { url ->
                prefs.ollamaApiUrl.set(url)
                Timber.d("Ollama URL updated: $url")
            }
            .autoDisposable(view.scope())
            .subscribe { url ->
                newState { copy(ollamaUrl = url) }
            }

        // Handle model selection
        view.modelSelected()
            .doOnNext { model ->
                prefs.ollamaModel.set(model)
                Timber.d("Ollama model selected: $model")
            }
            .autoDisposable(view.scope())
            .subscribe { model ->
                newState { copy(selectedModel = model) }
                view.showToast("Model selected: $model")
            }

        // Handle auto-reply to all toggle
        view.autoReplyToAllChanged()
            .doOnNext { enabled ->
                prefs.aiAutoReplyToAll.set(enabled)
                Timber.d("Auto-Reply to All: $enabled")
            }
            .autoDisposable(view.scope())
            .subscribe { enabled ->
                newState { copy(autoReplyToAll = enabled) }
                if (enabled) {
                    view.showToast("⚠️ Auto-reply is now active for ALL messages")
                } else {
                    view.showToast("Auto-reply disabled")
                }
            }

        // Handle test connection
        view.testConnectionClicks()
            .doOnNext { newState { copy(connectionStatus = ConnectionStatus.Testing, loadingModels = true) } }
            .withLatestFrom(state) { _, state -> state.ollamaUrl }
            .switchMap { url ->
                fetchOllamaModels.buildObservable(FetchOllamaModels.Params(url))
                    .toObservable()
            }
            .autoDisposable(view.scope())
            .subscribe(
                { models ->
                    Timber.d("Fetched ${models.size} models from Ollama")
                    newState {
                        copy(
                            availableModels = models,
                            connectionStatus = ConnectionStatus.Connected,
                            loadingModels = false
                        )
                    }
                    view.showToast("✓ Connected! Found ${models.size} models")

                    // Auto-show model picker if models were fetched
                    if (models.isNotEmpty()) {
                        view.showModelPicker(
                            models.map { it.name },
                            prefs.ollamaModel.get()
                        )
                    }
                },
                { error ->
                    Timber.e(error, "Failed to fetch models")
                    newState {
                        copy(
                            connectionStatus = ConnectionStatus.Failed,
                            loadingModels = false
                        )
                    }
                    view.showToast("Connection failed: ${error.message}")
                }
            )
    }
}
