// ui/screens/MainAppScreen.kt

package com.example.kontrog.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

// --- Логика Firebase Auth (Оставлена для кнопки выхода) ---
fun signOutUser() {
    Firebase.auth.signOut()
}
// --------------------------------------------------------

/**
 * 🔑 Основной компонент для Главного Экрана
 * Принимает NavController для навигации между экранами (Notifications/Profile).
 */
@Composable
fun MainScreen(navController: NavController) { // 🔑 NavController теперь здесь
    // Здесь мы по умолчанию будем показывать контент пользователя
    val role = "user"

    Scaffold(
        topBar = {
            // Передаем NavController в TopBar для обработки нажатий кнопок
            MainScreenTopBar(navController, role)
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Контент для пользователя (упрощенно)
            UserDashboardContent()
        }
    }
}

// --------------------------------------------------------
// --- Компоненты Top Bar ---
// --------------------------------------------------------

@Composable
fun MainScreenTopBar(navController: NavController, role: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Поле поиска
        SearchField(
            modifier = Modifier.weight(1f)
        )

        // 2. Кнопка Фильтра
        IconButton(
            onClick = { /* TODO: Открыть фильтр */ },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(Icons.Default.Create, contentDescription = "Фильтр")
        }

        // 3. Кнопка Уведомлений
        IconButton(
            onClick = {
                // Переход на отдельный экран уведомлений
                navController.navigate(Screen.Notifications.route)
            },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(Icons.Default.Notifications, contentDescription = "Уведомления")
        }

        // 4. Иконка Профиля (Переход на Аккаунт/Профиль)
        IconButton(
            onClick = {
                // Переход на вкладку "Аккаунт" (Profile) в Bottom Bar
                navController.navigate(BottomNavItem.Profile.route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(Icons.Default.Person, contentDescription = "Аккаунт")
        }
    }
}

@Composable
fun SearchField(modifier: Modifier = Modifier) {
    var text by remember { mutableStateOf("") }
    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        placeholder = { Text("Добрый день, Иван...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Поиск") },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF2E2E2E),
            unfocusedContainerColor = Color(0xFF2E2E2E),
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            cursorColor = Color.White
        ),
        modifier = modifier
            .height(50.dp)
            .padding(end = 8.dp)
    )
}

@Composable
fun UserDashboardContent() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Привет, Пользователь! (Главный экран)")
        Spacer(modifier = Modifier.height(16.dp))
        Text("Здесь будет дашборд с огнетушителями и журналами.", textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { signOutUser() }) {
            Text("Выйти из аккаунта (Logout)")
        }
    }
}