package com.khatm.client.domain.repositories

import androidx.lifecycle.LiveData
import com.khatm.client.domain.models.SettingsModel
import kotlinx.coroutines.Deferred

interface SettingsRepository {
    val settingsFromDbAsync : Deferred<SettingsModel?>
    fun storeToDbAsync(settings : SettingsModel) : Deferred<Boolean>
    suspend fun settingsFromServer(currentVersion: Int) : SettingsModel?
}