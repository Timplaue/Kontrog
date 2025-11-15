package com.example.kontrog.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.kontrog.RootDestinations
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

// --- Логика Firebase Auth ---
fun signOutUser() {
    Firebase.auth.signOut()
}

/**
 * 🔑 Экран Профиля, реализованный по дизайну.
 * Добавлен скроллинг, чтобы кнопка ВЫЙТИ не обрезалась.
 * @param rootNavController NavController корневого уровня для навигации после выхода (на экран Auth).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(rootNavController: NavController) {
    val scrollState = rememberScrollState() // Состояние для скроллинга

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White
                ),
                title = { Text("ПРОФИЛЬ", fontWeight = FontWeight.Bold) },
                // Кнопка "Назад" удалена, т.к. это экран Bottom Navigation
                navigationIcon = { /* Пусто */ }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color.Black)
                .verticalScroll(scrollState) // 💡 СДЕЛАНО СКРОЛЛЯЩИМСЯ
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- 1. Аватар и Имя ---
            ProfileHeader()
            Spacer(modifier = Modifier.height(24.dp))

            // --- 2. Раздел "ДАННЫЕ" ---
            SectionHeader(title = "ДАННЫЕ")
            ProfileInfoRow(label = "ЭЛ.ПОЧТА", value = "IVANOV@STERLITAMAK.RU")
            ProfileInfoRow(label = "ТЕЛЕФОН", value = "*79841071828")
            ProfileInfoRow(label = "ЛОГИН", value = "IVANOV_I", isLast = true)
            Spacer(modifier = Modifier.height(24.dp))

            // --- 3. Раздел "СТАТИСТИКА" ---
            SectionHeader(title = "СТАТИСТИКА")
            ProfileStatRow(label = "ПРИВЯЗАННЫЕ ОБЪЕКТЫ", value = "12", onClick = { /* TODO */ })
            ProfileStatRow(label = "ВЫПОЛНЕНО ПРОВЕРОК", value = "8", onClick = { /* TODO */ })
            ProfileStatRow(label = "ПРОСРОЧЕННЫХ СРЕДСТВ", value = "0", isLast = true, onClick = { /* TODO */ })
            Spacer(modifier = Modifier.height(24.dp))

            // --- 4. Раздел "НАСТРОЙКИ" ---
            SectionHeader(title = "НАСТРОЙКИ")
            ProfileNavRow(label = "ВЫБОР КАРТЫ: YANDEX", onClick = { /* TODO */ })
            ProfileNavRow(label = "ОФЛАЙН-СИНХРОНИЗАЦИЯ: ВКЛ", onClick = { /* TODO */ })
            ProfileNavRow(label = "СМЕНИТЬ PIN", onClick = { /* TODO */ })

            // --- 5. Кнопка ВЫЙТИ (Logout) ---
            ProfileNavRow(
                label = "ВЫЙТИ",
                isLogout = true,
                onClick = {
                    signOutUser()
                    // Переход на экран аутентификации с очисткой стека
                    rootNavController.navigate(RootDestinations.AUTH_ROUTE) {
                        popUpTo(rootNavController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                    }
                }
            )

            // --- 6. Версия ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "1.1 (20013)",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

// --------------------------------------------------------
// --- Вспомогательные Composable функции ---
// --------------------------------------------------------

@Composable
fun ProfileHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Заглушка для Аватара
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            //
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "ИВАНОВ ИВАН",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "ИНСПЕКТОР ПО НАДЗОРУ\nУПРАВЛЕНИЕ ПОЖНАДЗОРА Г.СТЕРЛИТАМАК",
            color = Color.Gray,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
        TextButton(onClick = { /* TODO: Изменить аватар */ }) {
            Text("ИЗМЕНИТЬ АВАТАР", color = Color(0xFF67B5FF), style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        color = Color.Gray,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
}

@Composable
fun ProfileInfoRow(label: String, value: String, isLast: Boolean = false) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = Color.White, style = MaterialTheme.typography.bodyMedium)
            Text(value, color = Color.White, style = MaterialTheme.typography.bodyMedium)
        }
        if (!isLast) {
            Divider(color = Color(0xFF2E2E2E), thickness = 1.dp)
        }
    }
}

@Composable
fun ProfileStatRow(label: String, value: String, isLast: Boolean = false, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick) // Сделано кликабельным
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = Color.White, style = MaterialTheme.typography.bodyMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(value, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                Icon(Icons.Default.ChevronRight, contentDescription = "Далее", tint = Color.Gray)
            }
        }
        if (!isLast) {
            Divider(color = Color(0xFF2E2E2E), thickness = 1.dp)
        }
    }
}

@Composable
fun ProfileNavRow(label: String, isLogout: Boolean = false, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                color = if (isLogout) Color(0xFF67B5FF) else Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
            if (!isLogout) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Далее", tint = Color.Gray)
            }
        }
        // Разделитель добавляется после каждой навигационной строки
        Divider(color = Color(0xFF2E2E2E), thickness = 1.dp)
    }
}