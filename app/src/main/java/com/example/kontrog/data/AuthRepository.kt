// AuthRepository.kt
package com.example.kontrog.data

import com.example.kontrog.data.models.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val db: FirebaseFirestore = Firebase.firestore
    private val usersCollection = db.collection("users")

    /**
     * Создаёт новую запись пользователя в Firestore
     */
    suspend fun createUserRecord(
        userId: String,
        email: String,
        phone: String,
        fullName: String = "",
        position: String = "",
        organizationId: String = "",
        responsibilityType: String = ""
    ) {
        val user = User(
            id = userId,
            userId = userId,
            email = email,
            phone = phone,
            fullName = fullName,
            position = position,
            organizationId = organizationId,
            responsibilityType = responsibilityType,
            role = "user",
            isPhoneVerified = false,
            createdAt = System.currentTimeMillis()
        )

        usersCollection
            .document(userId)
            .set(user)
            .await()
    }

    /**
     * Загружает данные пользователя по UID
     */
    suspend fun getUserData(userId: String): User? {
        return try {
            val document = usersCollection
                .document(userId)
                .get()
                .await()

            document.toObject<User>()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Обновляет статус верификации телефона
     */
    suspend fun markPhoneAsVerified(userId: String) {
        usersCollection
            .document(userId)
            .update("isPhoneVerified", true)
            .await()
    }

    /**
     * Обновляет данные пользователя (частичное обновление)
     */
    suspend fun updateUserData(userId: String, updates: Map<String, Any>) {
        usersCollection
            .document(userId)
            .set(updates, SetOptions.merge())
            .await()
    }

    /**
     * Проверяет существование пользователя по телефону
     */
    suspend fun checkPhoneExists(phone: String): Boolean {
        val query = usersCollection
            .whereEqualTo("phone", phone)
            .limit(1)
            .get()
            .await()

        return !query.isEmpty
    }
}