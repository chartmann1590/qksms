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
import androidx.appcompat.app.AlertDialog
import com.charles.messenger.BuildConfig
import com.charles.messenger.R
import com.charles.messenger.feature.main.MainActivity
import com.charles.messenger.manager.ChangelogManager
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.changelog_dialog.view.*

class ChangelogDialog(activity: MainActivity) {

    val moreClicks: Subject<Unit> = PublishSubject.create()

    private val dialog: AlertDialog
    private val adapter = ChangelogAdapter(activity)

    init {
        val layout = LayoutInflater.from(activity).inflate(R.layout.changelog_dialog, null)

        dialog = AlertDialog.Builder(activity)
                .setCancelable(true)
                .setView(layout)
                .create()

        layout.version.text = activity.getString(R.string.changelog_version, BuildConfig.VERSION_NAME)
        layout.changelog.adapter = adapter
        layout.more.setOnClickListener { dialog.dismiss(); moreClicks.onNext(Unit) }
        layout.dismiss.setOnClickListener { dialog.dismiss() }
    }

    fun show(changelog: ChangelogManager.CumulativeChangelog) {
        timber.log.Timber.d("Showing changelog dialog: added=${changelog.added.size}, improved=${changelog.improved.size}, fixed=${changelog.fixed.size}")
        adapter.setChangelog(changelog)
        dialog.show()
    }

}
