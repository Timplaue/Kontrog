package com.example.kontrog.data.models

import com.google.firebase.firestore.DocumentId

/**
 * Модель данных для Здания/Объекта.
 * Добавлены поля latitude и longitude для отображения на карте.
 */
data class Building(
    @DocumentId
    val id: String = "",
    val organizationId: String = "", // 🔑 Ключ: привязка к родительской Organization
    val address: String = "",
    val floors: Int = 0,
    val areaSqM: Double = 0.0,
    val type: String = "", // Например: "Офис", "Склад", "Производство"
    val description: String = "", // Дополнительное поле для описания

    // 💡 НОВЫЕ ПОЛЯ ДЛЯ КАРТЫ
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)