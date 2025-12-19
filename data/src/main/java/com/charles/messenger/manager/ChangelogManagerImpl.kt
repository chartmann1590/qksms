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
import timber.log.Timber
import javax.inject.Inject

class ChangelogManagerImpl @Inject constructor(
    private val context: Context,
    private val moshi: Moshi,
    private val prefs: Preferences
) : ChangelogManager {

    override fun didUpdate(): Boolean = prefs.changelogVersion.get() != context.versionCode

    override suspend fun getChangelog(): ChangelogManager.CumulativeChangelog {
        val listType = Types.newParameterizedType(List::class.java, Changeset::class.java)
        val adapter = moshi.adapter<List<Changeset>>(listType)

        return withContext(Dispatchers.IO) {
            try {
                val jsonText = context.assets.open("changelog.json").bufferedReader().use { it.readText() }
                val changelogs = adapter.fromJson(jsonText)
                    ?: emptyList<Changeset>()
                
                Timber.d("Loaded ${changelogs.size} changelog entries")
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
                Timber.e(e, "Error loading changelog")
                ChangelogManager.CumulativeChangelog(added = emptyList(), improved = emptyList(), fixed = emptyList())
            }
        }
    }

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


}
