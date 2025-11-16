package com.example.kontrog.data.models

import com.google.firebase.firestore.DocumentId

/**
 * Модель данных для Юридического лица/Организации.
 * Соответствует требованиям ТЗ: Название, ИНН, Адрес, возможность множественности.
 */
data class Organization(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val inn: String = "",
    val address: String = "",
    val ownerUid: String = "",
    val creationDate: Long = 0,
    val isActive: Boolean = true
)