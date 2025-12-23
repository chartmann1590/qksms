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
import javax.inject.Inject

class AiSettingsController : QkController<AiSettingsView, AiSettingsState, AiSettingsPresenter>(), AiSettingsView {

    @Inject override lateinit var presenter: AiSettingsPresenter

    private val urlChangedSubject: Subject<String> = PublishSubject.create()
    private val modelSelectedSubject: Subject<String> = PublishSubject.create()

    private lateinit var preferences: LinearLayout
    private lateinit var aiEnabled: PreferenceView
    private lateinit var ollamaUrl: PreferenceView
    private lateinit var modelSelection: PreferenceView
    private lateinit var testConnection: Button
    private lateinit var connectionStatus: TextView
    private lateinit var aiAutoReplyToAll: PreferenceView
    private lateinit var autoReplyWarning: TextView

    init {
        appComponent.inject(this)
        layoutRes = R.layout.ai_settings_controller
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        // #region agent log
        com.charles.messenger.util.DebugLogger.log(
            location = "AiSettingsController.kt:61",
            message = "onViewCreated started",
            hypothesisId = "H2"
        )
        // #endregion
        preferences = view.findViewById(R.id.preferences)
        aiEnabled = view.findViewById(R.id.aiEnabled)
        ollamaUrl = view.findViewById(R.id.ollamaUrl)
        modelSelection = view.findViewById(R.id.modelSelection)
        testConnection = view.findViewById(R.id.testConnection)
        connectionStatus = view.findViewById(R.id.connectionStatus)
        aiAutoReplyToAll = view.findViewById(R.id.aiAutoReplyToAll)
        autoReplyWarning = view.findViewById(R.id.autoReplyWarning)
        
        // #region agent log
        com.charles.messenger.util.DebugLogger.log(
            location = "AiSettingsController.kt:73",
            message = "All views initialized, calling bindIntents",
            data = mapOf("preferencesInitialized" to ::preferences.isInitialized),
            hypothesisId = "H2"
        )
        // #endregion
        // Bind intents AFTER all views are initialized
        presenter.bindIntents(this)
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        setTitle(R.string.ai_settings_title)
        showBackButton(true)

        // Handle clicks on switch preferences to toggle them
        aiEnabled.clicks().subscribe {
            val switch = aiEnabled.widget?.findViewById<QkSwitch>(R.id.checkbox)
            switch?.isChecked = !(switch?.isChecked ?: false)
        }

        aiAutoReplyToAll.clicks().subscribe {
            val switch = aiAutoReplyToAll.widget?.findViewById<QkSwitch>(R.id.checkbox)
            switch?.isChecked = !(switch?.isChecked ?: false)
        }
    }

    override fun preferenceClicks(): Observable<PreferenceView> {
        // #region agent log
        com.charles.messenger.util.DebugLogger.log(
            location = "AiSettingsController.kt:93",
            message = "preferenceClicks called",
            data = mapOf("preferencesInitialized" to ::preferences.isInitialized),
            hypothesisId = "H2"
        )
        // #endregion
        if (!::preferences.isInitialized) {
            // #region agent log
            com.charles.messenger.util.DebugLogger.log(
                location = "AiSettingsController.kt:96",
                message = "preferences not initialized, returning empty",
                hypothesisId = "H2"
            )
            // #endregion
            return Observable.empty()
        }
        // #region agent log
        com.charles.messenger.util.DebugLogger.log(
            location = "AiSettingsController.kt:100",
            message = "preferences initialized, building observable",
            data = mapOf("childCount" to preferences.childCount),
            hypothesisId = "H2"
        )
        // #endregion
        return (0 until preferences.childCount)
            .map { index -> preferences.getChildAt(index) }
            .mapNotNull { view -> view as? PreferenceView }
            .map { preference -> preference.clicks().map { preference } }
            .let { preferences -> Observable.merge(preferences) }
    }

    override fun testConnectionClicks(): Observable<Unit> = testConnection.clicks()

    override fun aiEnabledChanged(): Observable<Boolean> {
        return aiEnabled.widget?.let { widget ->
            (widget.findViewById<View>(R.id.checkbox) as? QkSwitch)
                ?.checkedChanges()
                ?.skipInitialValue()
        } ?: Observable.empty()
    }

    override fun ollamaUrlChanged(): Observable<String> = urlChangedSubject

    override fun modelSelected(): Observable<String> = modelSelectedSubject

    override fun autoReplyToAllChanged(): Observable<Boolean> {
        return aiAutoReplyToAll.widget?.let { widget ->
            (widget.findViewById<View>(R.id.checkbox) as? QkSwitch)
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
            (widget.findViewById<View>(R.id.checkbox) as? QkSwitch)?.isChecked = state.aiEnabled
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
            (widget.findViewById<View>(R.id.checkbox) as? QkSwitch)?.isChecked = state.autoReplyToAll
        }

        // Show/hide warning based on auto-reply state
        autoReplyWarning.visibility = if (state.autoReplyToAll) View.VISIBLE else View.GONE
    }
}
