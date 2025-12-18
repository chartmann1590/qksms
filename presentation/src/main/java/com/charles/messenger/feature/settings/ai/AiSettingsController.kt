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

import android.app.AlertDialog
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.checkedChanges
import com.charles.messenger.R
import com.charles.messenger.common.base.QkController
import com.charles.messenger.common.widget.PreferenceView
import com.charles.messenger.injection.appComponent
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.ai_settings_controller.*
import javax.inject.Inject

class AiSettingsController : QkController<AiSettingsView, AiSettingsState, AiSettingsPresenter>(), AiSettingsView {

    @Inject override lateinit var presenter: AiSettingsPresenter

    private val urlChangedSubject: Subject<String> = PublishSubject.create()
    private val modelSelectedSubject: Subject<String> = PublishSubject.create()

    init {
        appComponent.inject(this)
        layoutRes = R.layout.ai_settings_controller
    }

    override fun onViewCreated() {
        super.onViewCreated()
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        presenter.bindIntents(this)
        setTitle(R.string.ai_settings_title)
        showBackButton(true)
    }

    override fun preferenceClicks(): Observable<PreferenceView> = (0 until preferences.childCount)
        .map { index -> preferences.getChildAt(index) }
        .mapNotNull { view -> view as? PreferenceView }
        .map { preference -> preference.clicks().map { preference } }
        .let { preferences -> Observable.merge(preferences) }

    override fun testConnectionClicks(): Observable<Unit> = testConnection.clicks()

    override fun aiEnabledChanged(): Observable<Boolean> {
        return aiEnabled.widget?.let { widget ->
            (widget.findViewById<View>(R.id.toggle) as? android.widget.Switch)
                ?.checkedChanges()
                ?.skipInitialValue()
        } ?: Observable.empty()
    }

    override fun ollamaUrlChanged(): Observable<String> = urlChangedSubject

    override fun modelSelected(): Observable<String> = modelSelectedSubject

    override fun autoReplyToAllChanged(): Observable<Boolean> {
        return aiAutoReplyToAll.widget?.let { widget ->
            (widget.findViewById<View>(R.id.toggle) as? android.widget.Switch)
                ?.checkedChanges()
                ?.skipInitialValue()
        } ?: Observable.empty()
    }

    override fun showToast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    override fun showModelPicker(models: List<String>, selected: String) {
        if (models.isEmpty()) {
            showToast("No models available. Check connection and try again.")
            return
        }

        val selectedIndex = models.indexOf(selected).takeIf { it >= 0 } ?: 0

        AlertDialog.Builder(activity)
            .setTitle(R.string.ai_settings_model)
            .setSingleChoiceItems(models.toTypedArray(), selectedIndex) { dialog, which ->
                val model = models[which]
                modelSelectedSubject.onNext(model)
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun showUrlInputDialog(currentUrl: String) {
        val editText = EditText(activity).apply {
            setText(currentUrl)
            hint = "http://10.0.2.2:11434"
            setSingleLine()
        }

        AlertDialog.Builder(activity)
            .setTitle(R.string.ai_settings_ollama_url)
            .setView(editText)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val url = editText.text.toString().trim()
                if (url.isNotEmpty()) {
                    urlChangedSubject.onNext(url)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun render(state: AiSettingsState) {
        // Update AI enabled switch
        aiEnabled.widget?.let { widget ->
            (widget.findViewById<View>(R.id.toggle) as? android.widget.Switch)?.isChecked = state.aiEnabled
        }

        // Update Ollama URL summary
        ollamaUrl.summary = state.ollamaUrl.ifEmpty { "Not configured" }

        // Handle URL click to show input dialog
        ollamaUrl.clicks().subscribe {
            showUrlInputDialog(state.ollamaUrl)
        }

        // Update model selection summary
        modelSelection.summary = state.selectedModel.ifEmpty { "No model selected" }

        // Handle model click
        modelSelection.clicks().subscribe {
            if (state.availableModels.isNotEmpty()) {
                showModelPicker(state.availableModels.map { it.name }, state.selectedModel)
            } else {
                showToast("Please test connection first to load models")
            }
        }

        // Update connection status
        connectionStatus.text = when (state.connectionStatus) {
            ConnectionStatus.Unknown -> ""
            ConnectionStatus.Testing -> "Testing connection..."
            ConnectionStatus.Connected -> "✓ Connected (${state.availableModels.size} models available)"
            ConnectionStatus.Failed -> "✗ Connection failed"
        }

        connectionStatus.visibility = if (state.connectionStatus == ConnectionStatus.Unknown) {
            View.GONE
        } else {
            View.VISIBLE
        }

        // Update test button state
        testConnection.isEnabled = !state.loadingModels
        testConnection.alpha = if (state.loadingModels) 0.5f else 1.0f

        // Update auto-reply toggle
        aiAutoReplyToAll.widget?.let { widget ->
            (widget.findViewById<View>(R.id.toggle) as? android.widget.Switch)?.isChecked = state.autoReplyToAll
        }

        // Show/hide warning based on auto-reply state
        autoReplyWarning.visibility = if (state.autoReplyToAll) View.VISIBLE else View.GONE
    }
}
