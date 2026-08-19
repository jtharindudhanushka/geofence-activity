package com.group5.zonely.domain.repository

import com.group5.zonely.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<AppSettings>
    suspend fun update(transform: (AppSettings) -> AppSettings)
}
