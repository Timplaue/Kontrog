package com.example.kontrog.data.models

import com.google.firebase.firestore.DocumentId

data class ExtinguisherLog(
    @DocumentId
    val id: String = "",

    // 🔑 СВЯЗИ
    val extinguisherId: String = "",    // FK: Привязка к конкретноу огнетушителю
    val personId: String = "",          // FK: Ответственный исполнитель (из реестра "Люди")

    // 📄 ДАННЫЕ О РАБОТЕ
    val dateCompleted: Long = 0,        // Дата и время проведения работы
    val workType: String = "",          // Тип работы (Recharge, Inspection, MonthlyCheck, Repair)
    val result: String = "",            // Краткий результат ("Замечаний нет", "Перезаряжен", "Заменен")
    val comments: String = "",          // Детальные комментарии или замечания

    // 🗓️ ПОСЛЕДУЮЩИЕ ДАННЫЕ (Могут быть использованы для пересчета сроков)
    val newNextRechargeDate: Long? = null,    // Новая дата, если была перезарядка
    val newNextInspectionDate: Long? = null   // Новая дата, если было освидетельствование
)