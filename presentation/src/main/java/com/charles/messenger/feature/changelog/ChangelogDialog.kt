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
package com.charles.messenger.feature.changelog

import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.charles.messenger.BuildConfig
import com.charles.messenger.R
import com.charles.messenger.common.widget.QkTextView
import com.charles.messenger.feature.main.MainActivity
import com.charles.messenger.manager.ChangelogManager
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject

class ChangelogDialog(activity: MainActivity) {

    val moreClicks: Subject<Unit> = PublishSubject.create()

    private val dialog: AlertDialog
    private val adapter = ChangelogAdapter(activity)

    init {
        val layout = LayoutInflater.from(activity).inflate(R.layout.changelog_dialog, null)

        val versionView = layout.findViewById<QkTextView>(R.id.version)
        val changelogView = layout.findViewById<RecyclerView>(R.id.changelog)
        val moreView = layout.findViewById<View>(R.id.more)
        val dismissView = layout.findViewById<View>(R.id.dismiss)

        dialog = AlertDialog.Builder(activity)
                .setCancelable(true)
                .setView(layout)
                .create()

        versionView.text = activity.getString(R.string.changelog_version, BuildConfig.VERSION_NAME)
        changelogView.adapter = adapter
        moreView.setOnClickListener { dialog.dismiss(); moreClicks.onNext(Unit) }
        dismissView.setOnClickListener { dialog.dismiss() }
    }

    fun show(changelog: ChangelogManager.CumulativeChangelog) {
        timber.log.Timber.d("Showing changelog dialog: added=${changelog.added.size}, improved=${changelog.improved.size}, fixed=${changelog.fixed.size}")
        adapter.setChangelog(changelog)
        dialog.show()
    }

}
