package com.example.test.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.ProfileUiEvent
import com.example.test.ProfileUiState
import com.example.test.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()
    private val _events = Channel<ProfileUiEvent>()
    val events = _events.receiveAsFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                _uiState.update {
                    it.copy(
                        name = "Nurgalym (Hilt)",
                        bio = "Compose Master",
                        followerCount = 1050,
                        isFollowingMainUser = true,
                        isLoading = false,
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
                _events.send(ProfileUiEvent.ShowSnackbar("Ошибка загрузки профиля: ${e.message}"))
            }
        }
    }


    fun toggleMainUserFollow() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState.isFollowingMainUser) {
                _events.send(ProfileUiEvent.ShowUnfollowDialog)
            } else {
                _uiState.update { it.copy(isFollowingMainUser = true) }
                _events.send(ProfileUiEvent.ShowSnackbar("Вы подписались!"))
            }
        }
    }

    fun confirmUnfollowMainUser() {
        viewModelScope.launch {
            _uiState.update { it.copy(isFollowingMainUser = false) }
            _events.send(ProfileUiEvent.ShowSnackbar("Вы отписались!"))
        }
    }

    fun toggleFollowerFollow(followerId: Int, isFollowing: Boolean) {
        viewModelScope.launch {
            _events.send(ProfileUiEvent.ShowSnackbar("Обновление подписчика $followerId"))
        }
    }

    fun refresh() {
        loadProfile()
        viewModelScope.launch {
            _events.send(ProfileUiEvent.ShowSnackbar("Обновление данных..."))
        }
    }
}