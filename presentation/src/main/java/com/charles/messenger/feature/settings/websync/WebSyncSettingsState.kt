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

data class WebSyncSettingsState(
    val enabled: Boolean = false,
    val serverUrl: String = "",
    val username: String = "",
    val password: String = "",
    val connectionStatus: ConnectionStatus = ConnectionStatus.Unknown,
    val registrationStatus: RegistrationStatus = RegistrationStatus.NotRegistered,
    val syncInProgress: Boolean = false,
    val syncProgress: String = "",
    val lastSyncTimestamp: Long = 0
)

enum class ConnectionStatus {
    Unknown,
    Testing,
    Connected,
    Failed
}

enum class RegistrationStatus {
    NotRegistered,
    Registering,
    Registered,
    Failed
}
