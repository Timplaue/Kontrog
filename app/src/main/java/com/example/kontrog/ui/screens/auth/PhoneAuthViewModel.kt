// PhoneAuthViewModel.kt
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

sealed class PhoneAuthUiState {
    object Initial : PhoneAuthUiState()
    object CodeSent : PhoneAuthUiState()
    object Loading : PhoneAuthUiState()
    data class Error(val message: String) : PhoneAuthUiState()
    object Success : PhoneAuthUiState()
}

/**
 * ViewModel для верификации телефона через SMS (НЕ создаёт нового пользователя).
 * Использует linkWithCredential для привязки телефона к существующему аккаунту.
 */
class PhoneAuthViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<PhoneAuthUiState>(PhoneAuthUiState.Initial)
    val uiState: StateFlow<PhoneAuthUiState> = _uiState.asStateFlow()

    private var verificationId: String? = null
    var lastSentPhoneNumber: String? = null
        private set

    private val E164_PATTERN: Pattern = Pattern.compile("^\\+[1-9]\\d{1,14}\$")

    private fun normalizeToE164(raw: String, defaultCountryCode: String = "+7"): String {
        var s = raw.trim()
        s = s.replace("""[\s\-\(\)]""".toRegex(), "")

        if (s.startsWith("00")) {
            s = "+" + s.removePrefix("00")
        }

        if (s.startsWith("+")) return s

        return defaultCountryCode + s
    }

    private fun isValidE164(phone: String): Boolean {
        return E164_PATTERN.matcher(phone).matches()
    }

    /**
     * Отправляет код верификации на телефон.
     * ВАЖНО: Не создаёт нового пользователя в Auth!
     */
    fun sendVerificationCode(
        rawPhoneNumber: String,
        activity: Activity,
        defaultCountryCode: String = "+7"
    ) {
        _uiState.value = PhoneAuthUiState.Loading

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
                "Номер должен быть в формате E.164, например +71234567890"
            )
            return
        }

        lastSentPhoneNumber = phoneNumber

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d("PhoneAuth", "onVerificationCompleted - Auto verification")
                linkPhoneCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.e("PhoneAuth", "Verification Failed: ${e.message}", e)
                _uiState.value = PhoneAuthUiState.Error(
                    e.localizedMessage ?: "Ошибка отправки кода"
                )
            }

            override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                Log.d("PhoneAuth", "Code sent: verificationId=$id, phone=$phoneNumber")
                verificationId = id
                _uiState.value = PhoneAuthUiState.CodeSent
            }
        }

        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            _uiState.value = PhoneAuthUiState.Error("Пользователь не авторизован")
            return
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
            _uiState.value = PhoneAuthUiState.Error(
                e.localizedMessage ?: "Не удалось отправить код"
            )
        }
    }

    /**
     * Верифицирует введённый код и привязывает телефон к аккаунту
     */
    fun verifyCode(code: String) {
        val id = verificationId
        if (id == null) {
            _uiState.value = PhoneAuthUiState.Error("ID верификации отсутствует")
            return
        }

        _uiState.value = PhoneAuthUiState.Loading

        val credential = PhoneAuthProvider.getCredential(id, code)
        linkPhoneCredential(credential)
    }

    /**
     * Привязывает phone credential к существующему пользователю
     * (НЕ создаёт нового user в Auth!)
     */
    private fun linkPhoneCredential(credential: PhoneAuthCredential) {
        val currentUser = Firebase.auth.currentUser

        if (currentUser == null) {
            _uiState.value = PhoneAuthUiState.Error("Пользователь не найден")
            return
        }

        currentUser.updatePhoneNumber(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("PhoneAuth", "Phone number linked successfully")
                    _uiState.value = PhoneAuthUiState.Success
                } else {
                    val errorMessage = task.exception?.localizedMessage ?: "Неверный код"
                    Log.e("PhoneAuth", "Phone link failed: $errorMessage")
                    _uiState.value = PhoneAuthUiState.Error("Ошибка: $errorMessage")
                }
            }
    }

    fun resetState() {
        _uiState.value = PhoneAuthUiState.Initial
    }
}