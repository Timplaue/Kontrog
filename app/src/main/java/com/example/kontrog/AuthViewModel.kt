package com.example.kontrog

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kontrog.data.AuthRepository // 🔑 Импортируем репозиторий
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Состояния, которые будет отслеживать UI
data class AuthState(
    val isAuthenticated: Boolean = false,
    val role: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class AuthViewModel(
    // 🔑 Использование AuthRepository
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val auth = Firebase.auth
    // db больше не нужен, так как работа с Firestore перенесена в Repository

    private val _authState = MutableStateFlow(AuthState(isLoading = true))
    val authState: StateFlow<AuthState> = _authState

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        if (auth.currentUser != null) {
            fetchUserRole(auth.currentUser!!.uid)
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

            fetchUserRole(userId)

        } catch (e: Exception) {
            _authState.value = AuthState(isAuthenticated = false, isLoading = false, error = e.localizedMessage)
            Log.e("AuthViewModel", "Sign in failed", e)
        }
    }

    // ================== ЗАГРУЗКА РОЛИ ==================
    private fun fetchUserRole(userId: String) = viewModelScope.launch {
        _authState.value = _authState.value.copy(isLoading = true, error = null)
        try {
            // 🔑 Вызываем метод репозитория для загрузки роли
            val role = repository.getUserRole(userId)
            _authState.value = AuthState(isAuthenticated = true, role = role, isLoading = false)

        } catch (e: Exception) {
            _authState.value = AuthState(isAuthenticated = false, isLoading = false, error = "Failed to load role: ${e.localizedMessage}")
            auth.signOut() // Выходим, если не можем загрузить роль
            Log.e("AuthViewModel", "Error fetching role", e)
        }
    }

    fun signOut() {
        auth.signOut()
        _authState.value = AuthState(isAuthenticated = false, isLoading = false)
    }
}