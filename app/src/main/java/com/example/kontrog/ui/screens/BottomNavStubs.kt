package com.example.kontrog.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ProfileScreenStub() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Экран Профиля (Аккаунт) (Заглушка)")
    }
}

@Composable
fun MainScreenStub() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Главный экран (DashBoard). ДОБАВИТЬ TOP BAR ЗДЕСЬ!")
    }
}

@Composable
fun ObjectScreenStub() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Экран Объектов (Заглушка)")
    }
}

@Composable
fun MapScreenStub() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Экран Карты (Заглушка)")
    }
}

@Composable
fun DocsScreenStub() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Экран Документов (Заглушка)")
    }
}