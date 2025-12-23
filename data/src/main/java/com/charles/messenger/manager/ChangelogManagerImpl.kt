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
package com.charles.messenger.manager

import android.content.Context
import com.charles.messenger.common.util.extensions.versionCode
import com.charles.messenger.util.Preferences
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import javax.inject.Inject

class ChangelogManagerImpl @Inject constructor(
    private val context: Context,
    private val moshi: Moshi,
    private val prefs: Preferences,
    private val okHttpClient: OkHttpClient
) : ChangelogManager {

    companion object {
        private const val GITHUB_API_BASE = "https://api.github.com/repos/chartmann1590/qksms/releases"
        private const val TIMEOUT_SECONDS = 10L
    }

    override fun didUpdate(): Boolean = prefs.changelogVersion.get() != context.versionCode

    override suspend fun getChangelog(): ChangelogManager.CumulativeChangelog {
        return withContext(Dispatchers.IO) {
            // Try to fetch from GitHub first
            try {
                val githubChangelog = fetchChangelogFromGitHub()
                if (githubChangelog != null) {
                    Timber.d("Successfully loaded changelog from GitHub")
                    return@withContext githubChangelog
                }
            } catch (e: Exception) {
                Timber.w(e, "Failed to fetch changelog from GitHub, falling back to local")
            }

            // Fall back to local changelog.json
            try {
                val listType = Types.newParameterizedType(List::class.java, Changeset::class.java)
                val adapter = moshi.adapter<List<Changeset>>(listType)
                val jsonText = context.assets.open("changelog.json").bufferedReader().use { it.readText() }
                val changelogs = adapter.fromJson(jsonText)
                    ?: emptyList<Changeset>()
                
                Timber.d("Loaded ${changelogs.size} changelog entries from local file")
                Timber.d("Current version code: ${context.versionCode}, Last seen: ${prefs.changelogVersion.get()}")
                
                val filtered = changelogs
                    .sortedBy { changelog -> changelog.versionCode }
                    .filter { changelog ->
                        changelog.versionCode in prefs.changelogVersion.get().inc()..context.versionCode
                    }
                
                Timber.d("Filtered to ${filtered.size} changelog entries to show")

                ChangelogManager.CumulativeChangelog(
                        added = filtered.fold(listOf()) { acc, changelog -> acc + changelog.added.orEmpty()},
                        improved = filtered.fold(listOf()) { acc, changelog -> acc + changelog.improved.orEmpty()},
                        fixed = filtered.fold(listOf()) { acc, changelog -> acc + changelog.fixed.orEmpty()})
            } catch (e: Exception) {
                Timber.e(e, "Error loading local changelog")
                ChangelogManager.CumulativeChangelog(added = emptyList(), improved = emptyList(), fixed = emptyList())
            }
        }
    }

    private suspend fun fetchChangelogFromGitHub(): ChangelogManager.CumulativeChangelog? {
        return try {
            val lastSeenVersion = prefs.changelogVersion.get()
            val currentVersion = context.versionCode

            Timber.d("Fetching releases from GitHub (last seen: $lastSeenVersion, current: $currentVersion)")

            // Fetch latest releases (limit to 10 to avoid too much data)
            val url = "$GITHUB_API_BASE?per_page=10"
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            val response = okHttpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                Timber.w("GitHub API returned ${response.code}: ${response.message}")
                return null
            }

            val responseBody = response.body?.string()
                ?: return null

            val listType = Types.newParameterizedType(List::class.java, GitHubRelease::class.java)
            val adapter = moshi.adapter<List<GitHubRelease>>(listType)
            val releases = adapter.fromJson(responseBody)
                ?: return null

            Timber.d("Fetched ${releases.size} releases from GitHub")

            // Filter releases by version code (extract from tag name or body)
            // Parse release notes and accumulate changes
            val added = mutableListOf<String>()
            val improved = mutableListOf<String>()
            val fixed = mutableListOf<String>()

            // Get current version name for matching
            val currentVersionName = try {
                context.packageManager.getPackageInfo(context.packageName, 0).versionName
            } catch (e: Exception) {
                null
            }

            releases.forEach { release ->
                // Try to extract version code from tag or body
                val versionCode = extractVersionCodeFromTag(release.tagName)
                    ?: extractVersionCodeFromBody(release.body)
                
                if (versionCode != null) {
                    // Only include releases between last seen and current version
                    if (versionCode in lastSeenVersion.inc()..currentVersion) {
                        val parsed = parseReleaseNotes(release.body)
                        added.addAll(parsed.added)
                        improved.addAll(parsed.improved)
                        fixed.addAll(parsed.fixed)
                        Timber.d("Included release ${release.tagName} (versionCode: $versionCode)")
                    }
                } else {
                    // If we can't determine version code, check if tag matches current version name
                    // and include it if it's likely a recent release (only if we've updated)
                    val tagVersion = release.tagName.removePrefix("v")
                    if (tagVersion == currentVersionName && lastSeenVersion < currentVersion) {
                        val parsed = parseReleaseNotes(release.body)
                        added.addAll(parsed.added)
                        improved.addAll(parsed.improved)
                        fixed.addAll(parsed.fixed)
                        Timber.d("Included release ${release.tagName} (matched by version name)")
                    }
                }
            }

            if (added.isEmpty() && improved.isEmpty() && fixed.isEmpty()) {
                Timber.d("No new changes found in GitHub releases")
                return null
            }

            ChangelogManager.CumulativeChangelog(
                added = added,
                improved = improved,
                fixed = fixed
            )
        } catch (e: Exception) {
            Timber.e(e, "Error fetching changelog from GitHub")
            null
        }
    }

    private fun extractVersionCodeFromTag(tagName: String): Int? {
        // Try to extract version code from tag like "v3.9.6.2223"
        val parts = tagName.removePrefix("v").split(".")
        if (parts.size >= 4) {
            // Format: v3.9.6.2223
            return parts.last().toIntOrNull()
        }
        return null
    }

    private fun extractVersionCodeFromBody(body: String?): Int? {
        if (body.isNullOrBlank()) return null
        
        // Look for patterns like "Version Code: 2223" or "versionCode: 2223"
        val patterns = listOf(
            Regex("Version Code:\\s*(\\d+)", RegexOption.IGNORE_CASE),
            Regex("versionCode:\\s*(\\d+)", RegexOption.IGNORE_CASE),
            Regex("Build (\\d+)", RegexOption.IGNORE_CASE)
        )
        
        for (pattern in patterns) {
            val match = pattern.find(body)
            if (match != null) {
                return match.groupValues[1].toIntOrNull()
            }
        }
        
        return null
    }


    private fun parseReleaseNotes(body: String?): ParsedReleaseNotes {
        if (body.isNullOrBlank()) {
            return ParsedReleaseNotes(emptyList(), emptyList(), emptyList())
        }

        val added = mutableListOf<String>()
        val improved = mutableListOf<String>()
        val fixed = mutableListOf<String>()

        var currentSection: String? = null
        val lines = body.lines()

        for (line in lines) {
            val trimmed = line.trim()
            
            // Detect section headers
            when {
                trimmed.startsWith("### What's New", ignoreCase = true) -> {
                    currentSection = "added"
                }
                trimmed.startsWith("### Improvements", ignoreCase = true) -> {
                    currentSection = "improved"
                }
                trimmed.startsWith("### Fixed", ignoreCase = true) -> {
                    currentSection = "fixed"
                }
                trimmed.startsWith("### ", ignoreCase = true) -> {
                    // Other section, reset
                    currentSection = null
                }
                trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("• ") -> {
                    // List item
                    val item = trimmed.substring(2).trim()
                    if (item.isNotEmpty()) {
                        when (currentSection) {
                            "added" -> added.add(item)
                            "improved" -> improved.add(item)
                            "fixed" -> fixed.add(item)
                        }
                    }
                }
            }
        }

        return ParsedReleaseNotes(added, improved, fixed)
    }

    private data class ParsedReleaseNotes(
        val added: List<String>,
        val improved: List<String>,
        val fixed: List<String>
    )

    override fun markChangelogSeen() {
        prefs.changelogVersion.set(context.versionCode)
    }

    @JsonClass(generateAdapter = false)
    data class Changeset(
        @Json(name = "added") val added: List<String>?,
        @Json(name = "improved") val improved: List<String>?,
        @Json(name = "fixed") val fixed: List<String>?,
        @Json(name = "versionName") val versionName: String,
        @Json(name = "versionCode") val versionCode: Int
    )

    @JsonClass(generateAdapter = false)
    data class GitHubRelease(
        @Json(name = "tag_name") val tagName: String,
        @Json(name = "body") val body: String?,
        @Json(name = "published_at") val publishedAt: String?
    )

}
