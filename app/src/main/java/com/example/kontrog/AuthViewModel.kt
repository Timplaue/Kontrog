package com.example.kontrog

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kontrog.data.AuthRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AuthState(
    val isAuthenticated: Boolean = false,
    val role: String? = null,
    val phoneNumber: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val auth = Firebase.auth
    private val _authState = MutableStateFlow(AuthState(isLoading = true))
    val authState: StateFlow<AuthState> = _authState

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        if (auth.currentUser != null) {
            fetchUserRoleAndPhone(auth.currentUser!!.uid)
        } else {
            _authState.value = AuthState(isAuthenticated = false, isLoading = false)
        }
    }

    // ================== РЕГИСТРАЦИЯ ==================
    fun register(email: String, password: String, phone: String) = viewModelScope.launch {
        _authState.value = _authState.value.copy(isLoading = true, error = null)

        try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: throw Exception("UID is null after registration.")

            // 🔑 1. Вызываем метод репозитория для создания записи в Firestore
            repository.createUserRecord(userId, email, phone)

            _authState.value = AuthState(isAuthenticated = true, role = "user", isLoading = false)

        } catch (e: Exception) {
            // Если ошибка регистрации, удаляем пользователя (на всякий случай, если он был создан в Auth, но не в Firestore)
            auth.currentUser?.delete()
            _authState.value = AuthState(isAuthenticated = false, isLoading = false, error = e.localizedMessage)
            Log.e("AuthViewModel", "Registration failed", e)
        }
    }

    // ================== АВТОРИЗАЦИЯ ==================
    fun signIn(email: String, password: String) = viewModelScope.launch {
        _authState.value = _authState.value.copy(isLoading = true, error = null)

        try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: throw Exception("UID is null after sign in.")

            // 💡 Изменено: Загружаем роль И номер телефона
            fetchUserRoleAndPhone(userId)

        } catch (e: Exception) {
            _authState.value = AuthState(isAuthenticated = false, isLoading = false, error = e.localizedMessage)
            Log.e("AuthViewModel", "Sign in failed", e)
        }
    }

    // ================== ЗАГРУЗКА РОЛИ и ТЕЛЕФОНА ==================
    private fun fetchUserRoleAndPhone(userId: String) = viewModelScope.launch {
        _authState.value = _authState.value.copy(isLoading = true, error = null)
        try {
            // 🔑 Вызываем метод репозитория
            val userData = repository.getUserData(userId)

            if (userData != null) {
                val role = userData["role"] as? String
                val phone = userData["phone"] as? String

                if (role != null && phone != null) {
                    // Если телефон и роль есть, обновляем состояние
                    _authState.value = AuthState(
                        isAuthenticated = true,
                        role = role,
                        phoneNumber = phone, // 🔑 Сохраняем номер
                        isLoading = false
                    )
                } else {
                    // Пользовательский документ найден, но неполный
                    throw Exception("User data found, but missing role or phone number.")
                }
            } else {
                // Документ пользователя не найден в Firestore
                throw Exception("User record not found in database.")
            }

        } catch (e: Exception) {
            _authState.value = AuthState(
                isAuthenticated = false,
                isLoading = false,
                error = "Failed to load user data: ${e.localizedMessage}"
            )
            auth.signOut()
            Log.e("AuthViewModel", "Error fetching user data", e)
        }
    }

    fun signOut() {
        auth.signOut()
        _authState.value = AuthState(isAuthenticated = false, isLoading = false)
    }
}