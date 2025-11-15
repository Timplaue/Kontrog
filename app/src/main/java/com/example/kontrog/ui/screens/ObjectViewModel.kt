package com.example.kontrog.ui.screens

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

// Состояния для отображения на экране
sealed interface ObjectListUiState {
    data object Loading : ObjectListUiState
    data class Success(val buildings: List<Building>) : ObjectListUiState
    data class Error(val message: String) : ObjectListUiState
}

class ObjectViewModel(
    private val repository: FireSafetyRepository = RepositoryProvider.fireSafetyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ObjectListUiState>(ObjectListUiState.Loading)
    val uiState: StateFlow<ObjectListUiState> = _uiState.asStateFlow()

    init {
        loadBuildings()
    }

    private fun loadBuildings() {
        val currentUserId = Firebase.auth.currentUser?.uid ?: return // Проверка авторизации

        viewModelScope.launch {
            repository.getAllUserBuildings(currentUserId)
                .catch { e ->
                    // Вывод ошибки загрузки данных
                    _uiState.value = ObjectListUiState.Error("Ошибка загрузки объектов: ${e.message}")
                }
                .collect { buildingsList ->
                    // Если данные успешно загружены, обновляем состояние
                    _uiState.value = ObjectListUiState.Success(buildingsList)
                }
        }
    }
}