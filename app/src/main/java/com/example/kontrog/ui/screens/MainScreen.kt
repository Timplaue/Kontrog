package com.example.kontrog.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

fun signOutUser() {
    Firebase.auth.signOut()
}
@Composable
fun HomeScreen(role: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Главный экран для обычного пользователя ('user')
        Text("Привет, Пользователь! Твоя роль: $role")
        // Здесь будет дашборд с огнетушителями и журналами.

        // 🔑 КНОПКА ВЫХОДА
        Button(onClick = {
            signOutUser()
            // После вызова signOut() навигация должна автоматически перевести
            // пользователя на экран аутентификации, так как currentUser станет null.
        }) {
            Text("Выйти из аккаунта (Logout)")
        }
    }
}

@Composable
fun AdminScreen(role: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Главный экран для администратора ('admin')
        Text("Привет, АДМИН! Твоя роль: $role")
        // Здесь будут инструменты управления организациями и пользователями.

        // 🔑 КНОПКА ВЫХОДА
        Button(onClick = {
            signOutUser()
            // После вызова signOut() навигация должна автоматически перевести
            // пользователя на экран аутентификации.
        }) {
            Text("Выйти из аккаунта (Logout)")
        }
    }
}