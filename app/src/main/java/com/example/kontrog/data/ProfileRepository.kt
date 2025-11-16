package com.example.kontrog.data

import com.example.kontrog.ui.screens.ProfileData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class ProfileRepository {
    private val db: FirebaseFirestore = Firebase.firestore
    private val usersCollection = db.collection("users")

    /**
     * Загружает полную информацию профиля из единой коллекции 'users'
     */
    suspend fun getProfileData(userId: String): ProfileData {
        val userDoc = usersCollection.document(userId).get().await()

        if (!userDoc.exists()) {
            throw Exception("User record not found in database.")
        }

        val data = userDoc.data ?: throw Exception("User data is empty.")

        // Извлекаем данные из документа
        val fullName = data["fullName"] as? String ?: "Имя не указано"
        val email = data["email"] as? String ?: "Почта не указана"
        val phone = data["phone"] as? String ?: "Телефон не указан"
        val position = data["position"] as? String ?: "Должность не указана"
        val responsibilityType = data["responsibilityType"] as? String ?: ""

        val organizationId = data["organizationId"] as? String
        val organization = if (!organizationId.isNullOrEmpty()) {
            getOrganizationName(organizationId)
        } else {
            "Организация не указана"
        }

        val attachedObjects = 0
        val completedChecks = 0
        val overdueFunds = 0

        return ProfileData(
            fullName = fullName,
            email = email,
            phone = phone,
            position = position,
            organization = organization,
            responsibilityType = responsibilityType,
            attachedObjects = attachedObjects,
            completedChecks = completedChecks,
            overdueFunds = overdueFunds
        )
    }

    private suspend fun getOrganizationName(organizationId: String): String {
        return try {
            val orgDoc = db.collection("organizations")
                .document(organizationId)
                .get()
                .await()

            orgDoc.getString("name") ?: "Организация не найдена"
        } catch (e: Exception) {
            "Организация не найдена"
        }
    }

    suspend fun updateAvatar(userId: String, avatarUrl: String) {
        usersCollection
            .document(userId)
            .update("avatarUrl", avatarUrl)
            .await()
    }
}