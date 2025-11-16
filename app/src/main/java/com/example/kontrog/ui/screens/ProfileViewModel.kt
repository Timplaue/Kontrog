package com.example.kontrog

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kontrog.data.ProfileRepository
import com.example.kontrog.ui.screens.ProfileState
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: ProfileRepository = ProfileRepository()
) : ViewModel() {

    private val auth = Firebase.auth
    private val _profileState = MutableStateFlow(ProfileState())
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    init {
        loadProfileData()
    }

    fun loadProfileData() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _profileState.update {
                it.copy(
                    isLoading = false,
                    error = "Пользователь не авторизован"
                )
            }
            return
        }

        viewModelScope.launch {
            _profileState.update { it.copy(isLoading = true, error = null) }
            try {
                val data = repository.getProfileData(userId)
                _profileState.update {
                    it.copy(
                        data = data,
                        isLoading = false,
                        error = null
                    )
                }
                Log.d("ProfileViewModel", "Profile data loaded successfully")
            } catch (e: Exception) {
                _profileState.update {
                    it.copy(
                        isLoading = false,
                        error = "Ошибка загрузки данных: ${e.localizedMessage}"
                    )
                }
                Log.e("ProfileViewModel", "Failed to load profile data", e)
            }
        }
    }

    fun updateAvatar(avatarUrl: String) {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                repository.updateAvatar(userId, avatarUrl)
                loadProfileData()
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Failed to update avatar", e)
            }
        }
    }

    fun signOut() {
        auth.signOut()
    }
}