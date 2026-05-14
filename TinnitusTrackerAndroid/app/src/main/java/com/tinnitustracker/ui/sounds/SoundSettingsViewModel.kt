package com.tinnitustracker.ui.sounds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tinnitustracker.data.database.entities.SoundPreset
import com.tinnitustracker.data.repository.AudioRepository
import com.tinnitustracker.data.repository.SoundPresetRepository
import com.tinnitustracker.data.repository.UserSettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SoundSettingsViewModel(
    private val audioRepo: AudioRepository,
    private val settingsRepo: UserSettingsRepository,
    private val presetRepo: SoundPresetRepository
) : ViewModel() {

    // The active preset data flow
    val activePreset: StateFlow<SoundPreset?> = settingsRepo.activePresetId
        .flatMapLatest { id -> presetRepo.observeById(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun updateProcessingMode(mode: String) {
        viewModelScope.launch {
            activePreset.value?.let { preset ->
                presetRepo.savePreset(preset.copy(processingMode = mode))
            }
        }
    }

    fun updateColorNoise(color: String) {
        viewModelScope.launch {
            activePreset.value?.let { preset ->
                presetRepo.savePreset(preset.copy(colorNoise = color))
            }
        }
    }

    fun updateAmbientMix(source: String, volume: Float) {
        viewModelScope.launch {
            activePreset.value?.let { preset ->
                val newMix = preset.ambientMix.toMutableMap()
                newMix[source] = volume
                presetRepo.savePreset(preset.copy(ambientMix = newMix))
            }
        }
    }
}

class SoundSettingsViewModelFactory(
    private val audioRepo: AudioRepository,
    private val settingsRepo: UserSettingsRepository,
    private val presetRepo: SoundPresetRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SoundSettingsViewModel(audioRepo, settingsRepo, presetRepo) as T
    }
}
