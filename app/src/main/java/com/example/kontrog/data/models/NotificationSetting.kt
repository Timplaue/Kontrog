package com.example.kontrog.data.models

import com.google.firebase.firestore.DocumentId

/**
 * Настройки уведомлений для каждого пользователя.
 */
data class NotificationSetting(
    @DocumentId
    val id: String = "",

    val userId: String = "",            // FK: Привязка к пользователю

    // ⚙️ НАСТРОЙКИ СРОКОВ
    val notifyDaysBefore: List<Int> = listOf(30, 14, 7, 1), // Дни, за которые приходит уведомление
    val preferredTime: String = "09:00",                    // Предпочтительное время отправки уведомления (например, "09:00")

    // 💡 ТИПЫ УВЕДОМЛЕНИЙ (для огнетушителей)
    val notifyRecharge: Boolean = true,                     // Уведомлять о перезарядке
    val notifyInspection: Boolean = true                    // Уведомлять об освидетельствовании
)