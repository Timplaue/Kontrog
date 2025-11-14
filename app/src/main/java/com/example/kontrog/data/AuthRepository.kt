package com.example.kontrog.data

import com.google.firebase.firestore.FirebaseFirestore
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
     * Создает начальную запись пользователя в Firestore с ролью по умолчанию ('user').
     * @param userId Уникальный идентификатор пользователя из Firebase Authentication.
     * @param email Email пользователя.
     */
    suspend fun createUserRecord(userId: String, email: String, phone: String) {
        val userRoleData = hashMapOf(
            "email" to email,
            "phone" to phone,
            "role" to "user", // Роль по умолчанию
            "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )
        // Сохраняем данные пользователя, используя его UID как ID документа
        usersCollection
            .document(userId)
            .set(userRoleData)
            .await()
    }

    /**
     * Загружает роль пользователя из Firestore.
     * @param userId Уникальный идентификатор пользователя.
     * @return Строка с ролью ('user' или 'admin').
     */
    suspend fun getUserRole(userId: String): String {
        val document = usersCollection
            .document(userId)
            .get()
            .await()

        // Если документа нет, мы можем считать роль "user" (или обработать это как ошибку, но для начала 'user' безопаснее)
        return document.getString("role") ?: "user"
    }
}