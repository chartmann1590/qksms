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
            // #region agent log
            com.charles.messenger.util.DebugLogger.log(
                location = "OllamaRepositoryImpl.kt:48",
                message = "getAvailableModels called",
                data = mapOf("baseUrl" to baseUrl),
                hypothesisId = "H4"
            )
            // #endregion
            try {
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
            } catch (e: java.net.SocketException) {
                // #region agent log
                com.charles.messenger.util.DebugLogger.log(
                    location = "OllamaRepositoryImpl.kt:74",
                    message = "SocketException caught and handled",
                    data = mapOf("error" to e.message, "errorType" to "SocketException"),
                    hypothesisId = "H4"
                )
                // #endregion
                Timber.e(e, "Socket error while fetching models")
                throw Exception("Connection error: ${e.message}", e)
            } catch (e: android.system.ErrnoException) {
                // #region agent log
                com.charles.messenger.util.DebugLogger.log(
                    location = "OllamaRepositoryImpl.kt:77",
                    message = "ErrnoException caught and handled",
                    data = mapOf("error" to e.message, "errorType" to "ErrnoException"),
                    hypothesisId = "H4"
                )
                // #endregion
                Timber.e(e, "Connection error while fetching models")
                throw Exception("Connection refused: ${e.message}", e)
            } catch (e: java.net.UnknownHostException) {
                // #region agent log
                com.charles.messenger.util.DebugLogger.log(
                    location = "OllamaRepositoryImpl.kt:84",
                    message = "UnknownHostException caught and handled",
                    data = mapOf("error" to e.message, "errorType" to "UnknownHostException"),
                    hypothesisId = "H4"
                )
                // #endregion
                Timber.e(e, "Unknown host error while fetching models")
                throw Exception("Unknown host: ${e.message}", e)
            } catch (e: Exception) {
                // #region agent log
                com.charles.messenger.util.DebugLogger.log(
                    location = "OllamaRepositoryImpl.kt:91",
                    message = "Generic Exception caught",
                    data = mapOf("error" to e.message, "errorType" to e.javaClass.simpleName),
                    hypothesisId = "H4"
                )
                // #endregion
                Timber.e(e, "Error fetching models")
                throw e
            }
        }
    }

    override fun generateReplySuggestions(
        baseUrl: String,
        model: String,
        conversationContext: List<Message>
    ): Single<List<String>> {
        return Single.fromCallable {
            try {
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
            } catch (e: java.net.SocketException) {
                Timber.e(e, "Socket error while generating replies")
                throw Exception("Connection error: ${e.message}", e)
            } catch (e: android.system.ErrnoException) {
                Timber.e(e, "Connection error while generating replies")
                throw Exception("Connection refused: ${e.message}", e)
            } catch (e: java.net.UnknownHostException) {
                Timber.e(e, "Unknown host error while generating replies")
                throw Exception("Unknown host: ${e.message}", e)
            } catch (e: Exception) {
                Timber.e(e, "Error generating replies")
                throw e
            }
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

IMPORTANT: Return ONLY the reply suggestions, one per line, numbered 1-5. Do NOT include any explanations, introductions, or extra text. Keep each reply casual and natural like real text messages (1-2 sentences maximum).

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
                var suggestion = match.groupValues[1].trim()
                    .removeSurrounding("\"")
                    .removeSurrounding("'")

                // Remove any meta-text like "(casual)", "[friendly]", etc.
                suggestion = suggestion.replace(Regex("\\([^)]*\\)"), "")
                    .replace(Regex("\\[[^]]*\\]"), "")
                    .trim()

                // Clean markdown and LLM prefixes
                suggestion = cleanSuggestion(suggestion)

                if (suggestion.isNotEmpty() && suggestion.length > 3) {
                    suggestions.add(suggestion)
                }
            }
        }

        // Fallback: if no numbered suggestions found, split by newlines and take non-empty lines
        if (suggestions.isEmpty()) {
            response.lines()
                .map { cleanSuggestion(it.trim()) }
                .filter { it.isNotEmpty() && it.length > 5 }
                .take(5)
                .forEach { suggestions.add(it) }
        }

        // Limit to 5 suggestions
        return suggestions.take(5)
    }

    /**
     * Clean a suggestion by removing markdown formatting and common LLM prefixes
     */
    private fun cleanSuggestion(text: String): String {
        var cleaned = text

        // Remove markdown bold/italic markers
        cleaned = cleaned.replace(Regex("\\*\\*([^*]+)\\*\\*"), "$1")  // **text**
        cleaned = cleaned.replace(Regex("__([^_]+)__"), "$1")          // __text__
        cleaned = cleaned.replace(Regex("\\*([^*]+)\\*"), "$1")        // *text*
        cleaned = cleaned.replace(Regex("_([^_]+)_"), "$1")            // _text_

        // Remove surrounding quotes of all types
        cleaned = cleaned.removeSurrounding("\"")
        cleaned = cleaned.removeSurrounding("'")
        cleaned = cleaned.removeSurrounding("\u201C", "\u201D")  // curly double quotes
        cleaned = cleaned.removeSurrounding("\u2018", "\u2019")  // curly single quotes
        cleaned = cleaned.removeSurrounding("\u00AB", "\u00BB")  // guillemets

        // Remove any remaining stray quote characters at start/end
        cleaned = cleaned.trim('"', '\'', '\u201C', '\u201D', '\u2018', '\u2019')

        // Handle "Label: Actual reply" pattern - extract the part after colon if present
        // This handles cases like "Short encouragement: Keep going!" or "Friendly follow-up: How are you?"
        val colonIndex = cleaned.indexOf(':')
        if (colonIndex > 0 && colonIndex < cleaned.length - 1) {
            val afterColon = cleaned.substring(colonIndex + 1).trim()
            // Only use the part after colon if it looks like actual message content
            if (afterColon.length > 3 && !afterColon.contains(':')) {
                cleaned = afterColon
            }
        }

        // Final cleanup - remove quotes again
        cleaned = cleaned.removeSurrounding("\"")
        cleaned = cleaned.removeSurrounding("'")
        cleaned = cleaned.trim('"', '\'', '\u201C', '\u201D', '\u2018', '\u2019')

        return cleaned.trim()
    }
}
