package com.example.kontrog.data.repository

import com.example.kontrog.data.models.Building
import com.example.kontrog.data.models.Organization
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

// Константы для коллекций Firestore
private const val ORGANIZATIONS_COLLECTION = "organizations"
private const val BUILDINGS_COLLECTION = "buildings"
private const val FIELD_OWNER_UID = "ownerUid"
private const val FIELD_ORGANIZATION_ID = "organizationId"

/**
 * Реализация репозитория, использующая Firebase Firestore.
 */
class FireSafetyRepositoryImpl(
    private val db: FirebaseFirestore
) : FireSafetyRepository {

    // --- Операции с Организациями ---

    override fun getUserOrganizations(ownerUid: String): Flow<List<Organization>> {
        // Запрос к Firestore: ищем организации, где ownerUid совпадает
        return db.collection(ORGANIZATIONS_COLLECTION)
            .whereEqualTo(FIELD_OWNER_UID, ownerUid)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.toObject<Organization>() }
            }
    }

    override suspend fun saveOrganization(organization: Organization) {
        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            throw IllegalStateException("Пользователь не авторизован.")
        }

        // Добавляем ID текущего пользователя, если оно не было установлено
        val orgToSave = organization.copy(ownerUid = currentUser.uid)

        // Если ID пусто, Firestore сам сгенерирует новый документ
        if (orgToSave.id.isEmpty()) {
            db.collection(ORGANIZATIONS_COLLECTION).add(orgToSave).await()
        } else {
            // Обновляем существующий документ
            db.collection(ORGANIZATIONS_COLLECTION).document(orgToSave.id).set(orgToSave).await()
        }
    }

    override suspend fun deleteOrganization(organizationId: String) {
        // Удаление организации
        db.collection(ORGANIZATIONS_COLLECTION).document(organizationId).delete().await()
        // 💡 Важно: В реальном приложении здесь нужно также удалить все связанные Здания и Огнетушители!
    }

    // --- Операции со Зданиями ---

    override fun getBuildingsForOrganization(organizationId: String): Flow<List<Building>> {
        // Запрос к Firestore: ищем здания, привязанные к конкретной организации
        return db.collection(BUILDINGS_COLLECTION)
            .whereEqualTo(FIELD_ORGANIZATION_ID, organizationId)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.toObject<Building>() }
            }
    }

    override suspend fun saveBuilding(building: Building) {
        // Проверка: Здание должно быть привязано к существующей организации
        if (building.organizationId.isEmpty()) {
            throw IllegalArgumentException("Здание должно быть привязано к Организации.")
        }

        if (building.id.isEmpty()) {
            db.collection(BUILDINGS_COLLECTION).add(building).await()
        } else {
            db.collection(BUILDINGS_COLLECTION).document(building.id).set(building).await()
        }
    }

    override suspend fun deleteBuilding(buildingId: String) {
        // Удаление здания
        db.collection(BUILDINGS_COLLECTION).document(buildingId).delete().await()
        // 💡 Важно: В реальном приложении здесь нужно также удалить все связанные Огнетушители!
    }

    override fun getAllUserBuildings(ownerUid: String): Flow<List<Building>> {
        return db.collection(BUILDINGS_COLLECTION)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.toObject<Building>() }
            }
    }
}