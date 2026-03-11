package com.xtremeiptv

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xtremeiptv.core.common.manager.PreferenceManager
import com.xtremeiptv.core.common.manager.SessionManager
import com.xtremeiptv.core.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferenceManager: PreferenceManager,
    private val sessionManager: SessionManager,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _startDestination = MutableStateFlow("")
    val startDestination: StateFlow<String> = _startDestination.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun determineStartDestination() {
        viewModelScope.launch {
            _isLoading.value = true
            
            // Check if disclaimer accepted
            val disclaimerAccepted = preferenceManager.isDisclaimerAccepted()
            
            if (!disclaimerAccepted) {
                _startDestination.value = "disclaimer"
                _isLoading.value = false
                return@launch
            }
            
            // Check if there are any profiles
            val profiles = profileRepository.getProfiles().value
            if (profiles.isEmpty()) {
                _startDestination.value = "login"
                _isLoading.value = false
                return@launch
            }
            
            // Check if there's an active profile
            val activeProfile = profileRepository.getActiveProfile().value
            if (activeProfile != null) {
                sessionManager.setActiveProfile(activeProfile)
                _startDestination.value = "home"
            } else {
                _startDestination.value = "profile_selection"
            }
            
            _isLoading.value = false
        }
    }

    fun setActiveProfile(profileId: String) {
        viewModelScope.launch {
            profileRepository.setActiveProfile(profileId)
            profileRepository.getProfile(profileId).collect { profile ->
                profile?.let {
                    sessionManager.setActiveProfile(it)
                }
            }
        }
    }
}
