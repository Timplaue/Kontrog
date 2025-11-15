package com.example.kontrog.ui.screens.auth

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

// Состояние UI для экрана аутентификации по телефону
sealed class PhoneAuthUiState {
    object Initial : PhoneAuthUiState()
    object CodeSent : PhoneAuthUiState() // Код отправлен, ждем ввода
    object Loading : PhoneAuthUiState()
    data class Error(val message: String) : PhoneAuthUiState()
    object Success : PhoneAuthUiState()
}

class PhoneAuthViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<PhoneAuthUiState>(PhoneAuthUiState.Initial)
    val uiState: StateFlow<PhoneAuthUiState> = _uiState.asStateFlow()

    private var verificationId: String? = null

    /**
     * Хранит последний успешно нормализованный номер в E.164 (например "+79991234567"),
     * пригодный для повторной отправки.
     */
    var lastSentPhoneNumber: String? = null
        private set

    private val E164_PATTERN: Pattern = Pattern.compile("^\\+[1-9]\\d{1,14}\$")

    /**
     * Нормализует входной номер: убирает пробелы, скобки, тире; преобразует 00xxx -> +xxx;
     * если нет +, добавляет defaultCountryCode (например "+7").
     */
    private fun normalizeToE164(raw: String, defaultCountryCode: String = "+7"): String {
        var s = raw.trim()
        // убрать пробелы, скобки, тире
        s = s.replace("""[\s\-\(\)]""".toRegex(), "")

        // 00 -> +
        if (s.startsWith("00")) {
            s = "+" + s.removePrefix("00")
        }

        // если уже начинается с + — оставляем
        if (s.startsWith("+")) return s

        // иначе добавляем код страны по умолчанию
        return defaultCountryCode + s
    }

    private fun isValidE164(phone: String): Boolean {
        return E164_PATTERN.matcher(phone).matches()
    }

    /**
     * Отправка кода на номер. rawPhoneNumber может быть в любом читаемом формате,
     * функция попытается нормализовать его в E.164.
     *
     * defaultCountryCode — код, который будет добавлен если пользователь ввёл локальный номер без "+"
     * (например "+7" для России). При желании передавайте другой код.
     */
    fun sendVerificationCode(rawPhoneNumber: String, activity: Activity, defaultCountryCode: String = "+7") {
        _uiState.value = PhoneAuthUiState.Loading

        // Нормализуем и валидация
        val phoneNumber = try {
            normalizeToE164(rawPhoneNumber, defaultCountryCode)
        } catch (e: Exception) {
            Log.e("PhoneAuth", "Normalization failed for [$rawPhoneNumber]: ${e.message}", e)
            _uiState.value = PhoneAuthUiState.Error("Неверный формат номера.")
            return
        }

        if (!isValidE164(phoneNumber)) {
            Log.e("PhoneAuth", "Invalid E.164 format: $phoneNumber")
            _uiState.value = PhoneAuthUiState.Error(
                "Номер должен быть в формате E.164, например +71234567890. Введено: $rawPhoneNumber"
            )
            return
        }

        // Сохраняем нормализованный номер для повторных отправок
        lastSentPhoneNumber = phoneNumber

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Автоматическая верификация (например instant verification)
                Log.d("PhoneAuth", "onVerificationCompleted")
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.e("PhoneAuth", "Verification Failed: ${e.message}", e)
                _uiState.value = PhoneAuthUiState.Error(e.localizedMessage ?: "Ошибка верификации")
            }

            override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                // Код отправлен, сохраняем ID и переключаем UI на ввод кода
                Log.d("PhoneAuth", "onCodeSent: verificationId=$id, phone=$phoneNumber")
                verificationId = id
                _uiState.value = PhoneAuthUiState.CodeSent
            }
        }

        val options = PhoneAuthOptions.newBuilder(Firebase.auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        try {
            PhoneAuthProvider.verifyPhoneNumber(options)
        } catch (e: Exception) {
            Log.e("PhoneAuth", "verifyPhoneNumber exception", e)
            _uiState.value = PhoneAuthUiState.Error(e.localizedMessage ?: "Не удалось отправить код.")
        }
    }

    // 2. ВХОД С УЧЕТНЫМИ ДАННЫМИ
    fun signIn(code: String) {
        val id = verificationId
        if (id == null) {
            _uiState.value = PhoneAuthUiState.Error("ID верификации отсутствует.")
            return
        }

        _uiState.value = PhoneAuthUiState.Loading

        val credential = PhoneAuthProvider.getCredential(id, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        Firebase.auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _uiState.value = PhoneAuthUiState.Success
                } else {
                    val errorMessage = task.exception?.localizedMessage ?: "Неверный код."
                    _uiState.value = PhoneAuthUiState.Error("Ошибка входа: $errorMessage")
                    Log.e("PhoneAuth", "Sign In Failed: ${errorMessage}")
                }
            }
    }

    // Сброс состояния для повторной попытки
    fun resetState() {
        _uiState.value = PhoneAuthUiState.Initial
        // Не стираем lastSentPhoneNumber — это удобно для повторной отправки
    }
}
