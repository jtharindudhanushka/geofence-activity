package com.group5.zonely.data.repository

import com.group5.zonely.domain.model.AppSettings
import com.group5.zonely.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemorySettingsRepository @Inject constructor() : SettingsRepository {

    private val _settings = MutableStateFlow(AppSettings())
    override val settings: Flow<AppSettings> = _settings

    override suspend fun update(transform: (AppSettings) -> AppSettings) {
        _settings.update(transform)
    }
}
