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
package com.charles.messenger.feature.settings.websync

import com.charles.messenger.common.base.QkViewContract
import io.reactivex.Observable

interface WebSyncSettingsView : QkViewContract<WebSyncSettingsState> {
    fun webSyncEnabledChanged(): Observable<Boolean>
    fun serverUrlChanged(): Observable<String>
    fun usernameChanged(): Observable<String>
    fun passwordChanged(): Observable<String>
    fun registerAccountClicks(): Observable<Unit>
    fun testConnectionClicks(): Observable<Unit>
    fun performFullSyncClicks(): Observable<Unit>

    fun showServerUrlDialog(currentUrl: String)
    fun showUsernameDialog(currentUsername: String)
    fun showPasswordDialog()
    fun showToast(message: String)
}
