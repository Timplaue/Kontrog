package com.example.kontrog.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.material3.Text

@Composable
fun HomeScreen(role: String) {
    // Главный экран для обычного пользователя ('user')
    Text("Привет, Пользователь! Твоя роль: $role")
    // Здесь будет дашборд с огнетушителями и журналами.
}

@Composable
fun AdminScreen(role: String) {
    // Главный экран для администратора ('admin')
    Text("Привет, АДМИН! Твоя роль: $role")
    // Здесь будут инструменты управления организациями и пользователями.
}