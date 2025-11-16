package com.example.kontrog.data.models

import com.google.firebase.firestore.DocumentId

/**
 * Модель данных для Здания/Объекта.
 * Добавлены поля latitude и longitude для отображения на карте.
 */
data class Building(
    @DocumentId
    val id: String = "",
    val organizationId: String = "",
    val name: String = "",
    val address: String = "",
    val floors: Int = 0,
    val areaSqM: Double = 0.0,
    val type: String = "",
    val description: String = "",
    val responsiblePersonId: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)