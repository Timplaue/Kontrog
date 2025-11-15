package com.example.kontrog.data.models

import com.google.firebase.firestore.DocumentId

/**
 * Модель данных для Юридического лица/Организации.
 * Соответствует требованиям ТЗ: Название, ИНН, Адрес, возможность множественности.
 */
data class Organization(
    @DocumentId // Указывает, что это поле должно быть заполнено ID документа из Firestore
    val id: String = "",
    val name: String = "", // Название юридического лица
    val inn: String = "",  // ИНН
    val address: String = "", // Адрес организации
    val ownerUid: String = "" // ID пользователя, создавшего организацию (для безопасности)
)