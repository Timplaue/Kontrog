package com.example.kontrog.ui.screens

import android.util.Log // 💡 Добавляем для логирования ошибок
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kontrog.data.models.Building
import com.example.kontrog.data.repository.FireSafetyRepository
import com.example.kontrog.data.repository.RepositoryProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class MapViewModel(
    private val repository: FireSafetyRepository = RepositoryProvider.fireSafetyRepository
) : ViewModel() {

    private val _buildings = MutableStateFlow<List<Building>>(emptyList())
    val buildings: StateFlow<List<Building>> = _buildings.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadUserBuildings()
    }

    private fun loadUserBuildings() {
        val currentUserId = Firebase.auth.currentUser?.uid

        if (currentUserId == null) {
            Log.w("MapViewModel", "Current user ID is null. Cannot load user-specific buildings.")
            _isLoading.value = false
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            repository.getAllUserBuildings(currentUserId)
                .catch { e ->
                    Log.e("MapViewModel", "Error loading buildings: ${e.message}", e)
                    _buildings.value = emptyList()
                    _isLoading.value = false
                }
                .collect { list ->
                    _buildings.value = list
                    _isLoading.value = false
                    Log.d("MapViewModel", "Buildings loaded successfully: ${list.size}")
                }
        }
    }
}