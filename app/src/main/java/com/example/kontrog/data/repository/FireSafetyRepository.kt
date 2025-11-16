package com.example.kontrog.data.repository

import com.example.kontrog.data.models.Building
import com.example.kontrog.data.models.Organization
import kotlinx.coroutines.flow.Flow

interface FireSafetyRepository {

    fun getUserOrganizations(ownerUid: String): Flow<List<Organization>>

    suspend fun saveOrganization(organization: Organization)

    suspend fun deleteOrganization(organizationId: String)

    fun getBuildingsForOrganization(organizationId: String): Flow<List<Building>>

    suspend fun saveBuilding(building: Building)

    suspend fun deleteBuilding(buildingId: String)

    fun getAllUserBuildings(ownerUid: String): Flow<List<Building>>
}