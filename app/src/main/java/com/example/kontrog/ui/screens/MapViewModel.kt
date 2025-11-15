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

    // Состояние для хранения списка зданий
    private val _buildings = MutableStateFlow<List<Building>>(emptyList())
    val buildings: StateFlow<List<Building>> = _buildings.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadUserBuildings()
    }

    private fun loadUserBuildings() {
        // Получаем ID текущего пользователя
        // Если пользователь не авторизован (null), логика должна быть изменена
        // в репозитории для временной работы с тестовыми данными.
        val currentUserId = Firebase.auth.currentUser?.uid

        // ВАЖНО: Если аутентификация не настроена, и currentUserId == null,
        // но Firestore требует UID, загрузка не произойдет.
        // Для MVP-теста, если вы не вошли в систему, рассмотрите передачу
        // тестового ID, используемого в Firestore (например, "ORG-TEST-1"),
        // или временное отключение фильтрации в репозитории.

        if (currentUserId == null) {
            Log.w("MapViewModel", "Current user ID is null. Cannot load user-specific buildings.")
            _isLoading.value = false
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            // 💡 КОРРЕКЦИЯ: Включаем рабочую логику загрузки данных из репозитория.
            repository.getAllUserBuildings(currentUserId)
                .catch { e ->
                    // Логирование и вывод ошибки загрузки данных
                    Log.e("MapViewModel", "Error loading buildings: ${e.message}", e)
                    _buildings.value = emptyList() // Очищаем список при ошибке
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