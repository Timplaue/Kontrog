package com.example.kontrog.data.repository

import com.example.kontrog.data.models.Building
import com.example.kontrog.data.models.Organization
import kotlinx.coroutines.flow.Flow

/**
 * Интерфейс, определяющий операции с данными для MVP: Организации и Здания.
 */
interface FireSafetyRepository {

    // --- Операции с Организациями ---

    /**
     * Возвращает поток (Flow) всех организаций, принадлежащих текущему пользователю.
     * Flow идеально подходит для реактивного обновления UI при изменении данных в Firestore.
     */
    fun getUserOrganizations(ownerUid: String): Flow<List<Organization>>

    /**
     * Создает или обновляет данные организации.
     */
    suspend fun saveOrganization(organization: Organization)

    /**
     * Удаляет организацию по ID.
     */
    suspend fun deleteOrganization(organizationId: String)


    // --- Операции со Зданиями ---

    /**
     * Возвращает поток зданий, привязанных к конкретной организации.
     */
    fun getBuildingsForOrganization(organizationId: String): Flow<List<Building>>

    /**
     * Создает или обновляет данные здания.
     */
    suspend fun saveBuilding(building: Building)

    /**
     * Удаляет здание по ID.
     */
    suspend fun deleteBuilding(buildingId: String)
}