// User.kt
package com.example.kontrog.data.models

/**
 * Модель данных пользователя
 * Все поля имеют дефолтные значения для корректной работы с Firestore
 */
data class User(
    val id: String = "",
    val organizationId: String = "",
    val userId: String = "",

    val fullName: String = "",
    val position: String = "",

    val phone: String = "",
    val email: String = "",

    val responsibilityType: String = "",
    val role: String = "user",

    val createdAt: Long = 0L,
    val isPhoneVerified: Boolean = false
) {
    // Пустой конструктор для Firestore (требуется для toObject)
    constructor() : this(
        id = "",
        organizationId = "",
        userId = "",
        fullName = "",
        position = "",
        phone = "",
        email = "",
        responsibilityType = "",
        role = "user",
        createdAt = 0L,
        isPhoneVerified = false
    )
}