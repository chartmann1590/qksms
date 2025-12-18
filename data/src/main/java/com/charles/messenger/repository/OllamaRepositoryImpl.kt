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
package com.charles.messenger.repository

import com.charles.messenger.model.Message
import com.charles.messenger.model.OllamaGenerateRequest
import com.charles.messenger.model.OllamaGenerateResponse
import com.charles.messenger.model.OllamaModel
import com.charles.messenger.model.OllamaModelsResponse
import com.squareup.moshi.Moshi
import io.reactivex.Single
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OllamaRepositoryImpl @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val moshi: Moshi
) : OllamaRepository {

    companion object {
        private const val TAGS_ENDPOINT = "/api/tags"
        private const val GENERATE_ENDPOINT = "/api/generate"
        private const val JSON_MEDIA_TYPE = "application/json; charset=utf-8"
    }

    override fun getAvailableModels(baseUrl: String): Single<List<OllamaModel>> {
        return Single.fromCallable {
            val url = "${baseUrl.trimEnd('/')}$TAGS_ENDPOINT"
            Timber.d("Fetching models from: $url")

            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            val response = okHttpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                throw Exception("Failed to fetch models: ${response.code} ${response.message}")
            }

            val responseBody = response.body?.string()
                ?: throw Exception("Empty response body")

            val adapter = moshi.adapter(OllamaModelsResponse::class.java)
            val modelsResponse = adapter.fromJson(responseBody)
                ?: throw Exception("Failed to parse models response")

            Timber.d("Fetched ${modelsResponse.models.size} models")
            modelsResponse.models
        }
    }

    override fun generateReplySuggestions(
        baseUrl: String,
        model: String,
        conversationContext: List<Message>
    ): Single<List<String>> {
        return Single.fromCallable {
            val url = "${baseUrl.trimEnd('/')}$GENERATE_ENDPOINT"
            Timber.d("Generating replies from: $url with model: $model")

            // Build conversation context prompt
            val prompt = buildPrompt(conversationContext)
            Timber.d("Prompt: $prompt")

            // Create request
            val generateRequest = OllamaGenerateRequest(
                model = model,
                prompt = prompt,
                stream = false
            )

            val requestAdapter = moshi.adapter(OllamaGenerateRequest::class.java)
            val requestJson = requestAdapter.toJson(generateRequest)

            val request = Request.Builder()
                .url(url)
                .post(requestJson.toRequestBody(JSON_MEDIA_TYPE.toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                throw Exception("Failed to generate replies: ${response.code} ${response.message}")
            }

            val responseBody = response.body?.string()
                ?: throw Exception("Empty response body")

            val responseAdapter = moshi.adapter(OllamaGenerateResponse::class.java)
            val generateResponse = responseAdapter.fromJson(responseBody)
                ?: throw Exception("Failed to parse generate response")

            // Parse the response into multiple suggestions
            val suggestions = parseReplySuggestions(generateResponse.response)
            Timber.d("Generated ${suggestions.size} suggestions")

            suggestions
        }
    }

    /**
     * Build a prompt from conversation context
     */
    private fun buildPrompt(messages: List<Message>): String {
        if (messages.isEmpty()) {
            return "Generate 3-5 short, friendly reply suggestions for a text message conversation."
        }

        val conversationText = messages.takeLast(10).joinToString("\n") { message ->
            val sender = if (message.isMe()) "Me" else "Them"
            "$sender: ${message.body}"
        }

        return """
You are helping write replies to text messages. Based on this conversation, suggest 3-5 short, natural reply options.

Conversation:
$conversationText

Generate 3-5 brief reply suggestions (one per line, numbered 1-5). Keep replies casual and natural like real text messages. Each reply should be 1-2 sentences maximum.

Reply suggestions:
        """.trimIndent()
    }

    /**
     * Parse AI response into list of reply suggestions
     */
    private fun parseReplySuggestions(response: String): List<String> {
        // Split by lines and filter for numbered suggestions
        val suggestions = mutableListOf<String>()

        response.lines().forEach { line ->
            val trimmed = line.trim()

            // Match patterns like "1. Text" or "1) Text" or just numbered lines
            val numberPrefixRegex = Regex("^\\d+[.)]\\s*(.+)")
            val match = numberPrefixRegex.find(trimmed)

            if (match != null) {
                val suggestion = match.groupValues[1].trim()
                    .removeSurrounding("\"")
                    .removeSurrounding("'")
                if (suggestion.isNotEmpty()) {
                    suggestions.add(suggestion)
                }
            }
        }

        // Fallback: if no numbered suggestions found, split by newlines and take non-empty lines
        if (suggestions.isEmpty()) {
            response.lines()
                .map { it.trim() }
                .filter { it.isNotEmpty() && it.length > 5 }
                .take(5)
                .forEach { suggestions.add(it) }
        }

        // Limit to 5 suggestions
        return suggestions.take(5)
    }
}
