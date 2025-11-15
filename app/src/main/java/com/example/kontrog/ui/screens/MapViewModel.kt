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
        val currentUserId = Firebase.auth.currentUser?.uid ?: return

        // ВАЖНО: Репозиторий пока не имеет метода для получения *всех* зданий пользователя.
        // Пока мы берем все организации пользователя, ищем здания в каждой.
        // Но для простоты MVP, мы временно предположим, что все здания принадлежат
        // первой организации, или модифицируем запрос.

        // 💡 Временное решение: Создадим универсальный метод в репозитории.
        // Однако, учитывая, что здания привязаны к OrganizationId,
        // наиболее корректно - получить все организации пользователя,
        // и для каждой организации получить ее здания.

        // Для упрощения, давайте пока *предположим*, что у пользователя есть одна
        // главная организация, или что у нас есть некий "универсальный" запрос.
        // Для корректной работы здесь нужна реализация в FireSafetyRepositoryImpl,
        // которая собирает все здания по всем организациям пользователя.

        _isLoading.value = true
        viewModelScope.launch {
            try {
                // ПРИМЕЧАНИЕ: Этот код будет работать, если вы обновите FireSafetyRepository
                // и FireSafetyRepositoryImpl, чтобы добавить метод,
                // который возвращает ВСЕ здания пользователя.
                // Так как его нет, мы пока будем работать с заглушкой (см. Шаг 2).

                // ⬇️ ПРОВЕРКА: ЕСЛИ ВЫ ДОБАВИЛИ МЕТОД:
                // repository.getAllUserBuildings(currentUserId)
                //      .catch { error -> /* Обработка ошибки */ }
                //      .collect { list ->
                //          _buildings.value = list
                //          _isLoading.value = false
                //      }

            } catch (e: Exception) {
                // Логирование или вывод ошибки
                _isLoading.value = false
            }
        }
    }
}