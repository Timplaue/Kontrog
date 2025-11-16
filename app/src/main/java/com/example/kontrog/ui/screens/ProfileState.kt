package com.example.kontrog.ui.screens

data class ProfileData(
    val fullName: String = "Неизвестно",
    val email: String = "Неизвестно",
    val phone: String = "Неизвестно",
    val position: String = "Нет должности",
    val organization: String = "Нет организации",
    val responsibilityType: String = "",

    val attachedObjects: Int = 0,
    val completedChecks: Int = 0,
    val overdueFunds: Int = 0
)

data class ProfileState(
    val data: ProfileData = ProfileData(),
    val isLoading: Boolean = true,
    val error: String? = null
)