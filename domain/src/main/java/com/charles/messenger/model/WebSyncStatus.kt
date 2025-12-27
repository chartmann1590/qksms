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

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class WebSyncStatus : RealmObject() {
    @PrimaryKey var id: Int = 0
    var syncToken: String = ""
    var lastFullSyncTimestamp: Long = 0
    var lastIncrementalSyncTimestamp: Long = 0
    var syncInProgress: Boolean = false
    var totalMessagesSynced: Int = 0
}
