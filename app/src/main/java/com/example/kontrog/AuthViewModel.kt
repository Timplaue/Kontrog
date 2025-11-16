// AuthViewModel.kt
package com.example.kontrog

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kontrog.data.AuthRepository
import com.example.kontrog.data.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AuthState(
    val isAuthenticated: Boolean = false,
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val needsPhoneVerification: Boolean = false
)

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth
    private val _authState = MutableStateFlow(AuthState(isLoading = true))
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            fetchUserData(currentUser.uid)
        } else {
            _authState.value = AuthState(isAuthenticated = false, isLoading = false)
        }
    }

    // ================== РЕГИСТРАЦИЯ ==================
    fun register(
        email: String,
        password: String,
        phone: String,
        fullName: String = "",
        position: String = "",
        organizationId: String = "",
        responsibilityType: String = ""
    ) = viewModelScope.launch {
        _authState.value = _authState.value.copy(isLoading = true, error = null)

        try {
            // 1. Проверяем, не занят ли телефон
            if (repository.checkPhoneExists(phone)) {
                throw Exception("Этот номер телефона уже зарегистрирован")
            }

            // 2. Создаём пользователя в Firebase Auth
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: throw Exception("UID is null after registration.")

            // 3. Создаём запись в Firestore
            repository.createUserRecord(
                userId = userId,
                email = email,
                phone = phone,
                fullName = fullName,
                position = position,
                organizationId = organizationId,
                responsibilityType = responsibilityType
            )

            // 4. Загружаем данные пользователя
            fetchUserData(userId)

        } catch (e: Exception) {
            // Откатываем Auth если что-то пошло не так
            try {
                auth.currentUser?.delete()?.await()
            } catch (deleteException: Exception) {
                Log.e("AuthViewModel", "Failed to delete user after error", deleteException)
            }

            _authState.value = AuthState(
                isAuthenticated = false,
                isLoading = false,
                error = e.localizedMessage ?: "Ошибка регистрации"
            )
            Log.e("AuthViewModel", "Registration failed", e)
        }
    }

    // ================== АВТОРИЗАЦИЯ ==================
    fun signIn(email: String, password: String) = viewModelScope.launch {
        _authState.value = _authState.value.copy(isLoading = true, error = null)

        try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: throw Exception("UID is null after sign in.")

            fetchUserData(userId)

        } catch (e: Exception) {
            _authState.value = AuthState(
                isAuthenticated = false,
                isLoading = false,
                error = e.localizedMessage ?: "Ошибка входа"
            )
            Log.e("AuthViewModel", "Sign in failed", e)
        }
    }

    // ================== ЗАГРУЗКА ДАННЫХ ПОЛЬЗОВАТЕЛЯ ==================
    private fun fetchUserData(userId: String) = viewModelScope.launch {
        _authState.value = _authState.value.copy(isLoading = true, error = null)
        try {
            val userData = repository.getUserData(userId)

            if (userData != null) {
                _authState.value = AuthState(
                    isAuthenticated = true,
                    user = userData,
                    isLoading = false,
                    needsPhoneVerification = !userData.isPhoneVerified
                )
            } else {
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

    // ================== ПОДТВЕРЖДЕНИЕ ТЕЛЕФОНА ==================
    fun markPhoneVerified() = viewModelScope.launch {
        try {
            val userId = auth.currentUser?.uid ?: return@launch
            repository.markPhoneAsVerified(userId)

            // Обновляем локальное состояние
            val currentUser = _authState.value.user
            if (currentUser != null) {
                _authState.value = _authState.value.copy(
                    user = currentUser.copy(isPhoneVerified = true),
                    needsPhoneVerification = false
                )
            }
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Failed to mark phone as verified", e)
        }
    }

    // ================== ВЫХОД ==================
    fun signOut() {
        auth.signOut()
        _authState.value = AuthState(isAuthenticated = false, isLoading = false)
    }

    // ================== ГЕТТЕРЫ ==================
    fun getPhoneNumber(): String? = _authState.value.user?.phone
    fun getRole(): String? = _authState.value.user?.role
    fun getUserId(): String? = auth.currentUser?.uid
    fun getCurrentUser(): User? = _authState.value.user
}