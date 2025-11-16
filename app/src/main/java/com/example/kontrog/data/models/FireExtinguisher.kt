package com.example.kontrog.data.models

import com.google.firebase.firestore.DocumentId

data class FireExtinguisher(
    @DocumentId
    val id: String = "",

    // 🔑 СВЯЗИ
    val buildingId: String = "",         // FK: Привязка к зданию/объекту

    // 📄 ОСНОВНЫЕ ДАННЫЕ
    val inventoryNumber: String = "",    // Порядковый/инвентарный номер
    val locationRoom: String = "",       // Конкретное помещение установки
    val type: String = "",               // Тип (ОП-4, ОУ-5, ОВ-2 и т.д.)
    val manufacturer: String = "",       // Производитель (добавлено для полноты)
    val dateCommissioned: Long = 0,      // Дата ввода в эксплуатацию (в формате Timestamp/Long)

    // 🗓️ КРИТИЧЕСКИЕ СРОКИ (Timestamp/Long)
    val nextRechargeDate: Long = 0,      // Срок очередной перезарядки
    val nextInspectionDate: Long = 0,    // Срок следующего освидетельствования

    // 🚦 СТАТУС
    val status: String = "OK"            // Статус (OK, SoonExpired, Expired, OutOfService)
)