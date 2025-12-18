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
package com.charles.messenger.model

import com.squareup.moshi.Json

/**
 * Represents an Ollama AI model
 */
data class OllamaModel(
    @Json(name = "name") val name: String,
    @Json(name = "size") val size: Long,
    @Json(name = "modified_at") val modifiedAt: String
)

/**
 * Response from Ollama /api/tags endpoint
 */
data class OllamaModelsResponse(
    @Json(name = "models") val models: List<OllamaModel>
)

/**
 * Request to Ollama /api/generate endpoint
 */
data class OllamaGenerateRequest(
    @Json(name = "model") val model: String,
    @Json(name = "prompt") val prompt: String,
    @Json(name = "stream") val stream: Boolean = false
)

/**
 * Response from Ollama /api/generate endpoint
 */
data class OllamaGenerateResponse(
    @Json(name = "model") val model: String,
    @Json(name = "response") val response: String,
    @Json(name = "done") val done: Boolean
)
