package com.example.kontrog.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

/**
 * Репозиторий для всех операций, связанных с данными пользователя в Firestore.
 */
class AuthRepository {

    private val db: FirebaseFirestore = Firebase.firestore
    private val usersCollection = db.collection("users")

    /**
     * Создает начальную запись пользователя в Firestore.
     * @param userId Уникальный идентификатор пользователя из Firebase Authentication.
     * @param email Email пользователя.
     * @param phone Номер телефона пользователя (для 2FA).
     */
    suspend fun createUserRecord(userId: String, email: String, phone: String) {
        val userRoleData = hashMapOf(
            "email" to email,
            "phone" to phone,
            "role" to "user", // Роль по умолчанию
            "createdAt" to FieldValue.serverTimestamp() // 💡 Используем FieldValue из импорта
        )
        usersCollection
            .document(userId)
            .set(userRoleData)
            .await()
    }

    /**
     * Загружает все необходимые данные пользователя (роль, телефон) из Firestore.
     * Этот метод заменяет getUserRole, чтобы предоставить номер телефона для 2FA.
     * * @param userId Уникальный идентификатор пользователя.
     * @return Map<String, Any>? с данными ('role', 'phone', 'email'), или null, если документ не существует.
     */
    suspend fun getUserData(userId: String): Map<String, Any>? {
        val document = usersCollection
            .document(userId)
            .get()
            .await()

        return if (document.exists()) {
            // Возвращаем данные как Map<String, Any>
            document.data
        } else {
            null
        }
    }
}