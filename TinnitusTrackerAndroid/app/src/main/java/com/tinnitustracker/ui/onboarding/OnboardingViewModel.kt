package com.tinnitustracker.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tinnitustracker.data.repository.UserSettingsRepository
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val repo: UserSettingsRepository
) : ViewModel() {

    /** Called from either the final-page "시작하기" button or the per-page Skip. */
    fun completeOnboarding(onPersisted: () -> Unit) {
        viewModelScope.launch {
            repo.setOnboardingComplete(true)
            onPersisted()
        }
    }
}

class OnboardingViewModelFactory(
    private val repo: UserSettingsRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(OnboardingViewModel::class.java))
        return OnboardingViewModel(repo) as T
    }
}
