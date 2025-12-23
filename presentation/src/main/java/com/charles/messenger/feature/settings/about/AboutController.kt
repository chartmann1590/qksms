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
package com.charles.messenger.feature.settings.about

import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding2.view.clicks
import com.charles.messenger.BuildConfig
import com.charles.messenger.R
import com.charles.messenger.common.base.QkController
import com.charles.messenger.common.widget.PreferenceView
import com.charles.messenger.injection.appComponent
import io.reactivex.Observable
import javax.inject.Inject
import com.charles.messenger.*

class AboutController : QkController<AboutView, Unit, AboutPresenter>(), AboutView {

    @Inject override lateinit var presenter: AboutPresenter

    private lateinit var version: PreferenceView
    private lateinit var preferences: ViewGroup

    init {
        appComponent.inject(this)
        layoutRes = R.layout.about_controller
    }

    override fun onViewCreated(view: View) {
        version = view.findViewById(R.id.version)
        preferences = view.findViewById(R.id.preferences)

        version.summary = BuildConfig.VERSION_NAME
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        presenter.bindIntents(this)
        setTitle(R.string.about_title)
        showBackButton(true)
    }

    override fun preferenceClicks(): Observable<PreferenceView> = (0 until preferences.childCount)
            .map { index -> preferences.getChildAt(index) }
            .mapNotNull { view -> view as? PreferenceView }
            .map { preference -> preference.clicks().map { preference } }
            .let { preferences -> Observable.merge(preferences) }

    override fun render(state: Unit) {
        // No special rendering required
    }

}