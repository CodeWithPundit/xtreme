package com.xtremeiptv.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xtremeiptv.core.data.repository.ProfileRepository
import com.xtremeiptv.core.data.repository.StreamRepository
import com.xtremeiptv.core.domain.model.Profile
import com.xtremeiptv.core.domain.model.StreamModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val streamRepository: StreamRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            combine(
                profileRepository.getActiveProfile(),
                streamRepository.getLiveStreams(""),
                streamRepository.getMovies(""),
                streamRepository.getSeries("")
            ) { profile, liveStreams, movies, series ->
                HomeUiState(
                    activeProfile = profile,
                    featuredStreams = liveStreams.take(5),
                    liveStreams = liveStreams,
                    recentMovies = movies.take(10),
                    popularSeries = series.take(10),
                    continueWatching = getContinueWatching(),
                    recommendations = getRecommendations()
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    private suspend fun getContinueWatching(): List<StreamModel> {
        // Implement continue watching logic
        return emptyList()
    }

    private suspend fun getRecommendations(): List<StreamModel> {
        // Implement recommendation logic
        return emptyList()
    }

    fun refreshData() {
        loadHomeData()
    }
}

data class HomeUiState(
    val activeProfile: Profile? = null,
    val featuredStreams: List<StreamModel> = emptyList(),
    val continueWatching: List<StreamModel> = emptyList(),
    val recommendations: List<StreamModel> = emptyList(),
    val liveStreams: List<StreamModel> = emptyList(),
    val recentMovies: List<StreamModel> = emptyList(),
    val popularSeries: List<StreamModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
