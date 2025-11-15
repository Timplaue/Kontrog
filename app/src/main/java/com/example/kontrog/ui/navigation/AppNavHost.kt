package com.example.kontrog.ui.navigation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold

// --- 1. Определение маршрутов (Routes) и элементов нижнего бара (Bottom Tabs) ---

/**
 * 🗺️ Определяет все уникальные маршруты в приложении.
 */
sealed class Screen(val route: String) {
    // Root Screens (для корневого NavHost в MainActivity)
    object Notifications : Screen("notifications_route")

    // Bottom Tab Screens (для вложенного NavHost)
    object Main : Screen("main_tab_route")
    object Object : Screen("object_tab_route")
    object Map : Screen("map_tab_route")
    object Docs : Screen("docs_tab_route")
    object Profile : Screen("profile_tab_route")
}

/**
 * 🎨 Элементы, отображаемые в BottomNavigationBar.
 */
sealed class BottomTab(
    val screen: Screen, // Ссылка на маршрут из класса Screen
    val icon: ImageVector,
    val label: String
) {
    object Main : BottomTab(Screen.Main, Icons.Filled.Home, "Главная")
    object Object : BottomTab(Screen.Object, Icons.Filled.LocationOn, "Объект")
    object Map : BottomTab(Screen.Map, Icons.Filled.LocationOn, "Карта") // Изменил иконку
    object Docs : BottomTab(Screen.Docs, Icons.Filled.LocationOn, "Документы") // Изменил иконку
    object Profile : BottomTab(Screen.Profile, Icons.Filled.Person, "Профиль")
}

val bottomNavItems = listOf(
    BottomTab.Main,
    BottomTab.Object,
    BottomTab.Map,
    BottomTab.Docs,
    BottomTab.Profile
)

// --- 2. Заглушки экранов (Stubs) ---

@Composable
fun NotificationsScreen(navController: NavHostController) {
    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Экран Уведомлений", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.padding(8.dp))
            Button(onClick = { navController.popBackStack() }) {
                Text("Назад к главному экрану")
            }
        }
    }
}

@Composable
fun MainScreen(rootNavController: NavHostController) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Главный Экран (Вкладка)", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.padding(8.dp))
            // 💡 Переход на NotificationsScreen через корневой контроллер
            Button(onClick = { rootNavController.navigate(Screen.Notifications.route) }) {
                Text("Открыть Уведомления (Root-переход)")
            }
        }
    }
}

@Composable
fun ObjectScreenStub() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Объект Экран", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun MapScreenStub() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Карта Экран", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun DocsScreenStub() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Документы Экран", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun ProfileScreenStub() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Профиль Экран", style = MaterialTheme.typography.headlineMedium)
    }
}

// --- 3. Компоненты навигации (Navigation Components) ---

/**
 * 🚀 ГЛАВНЫЙ КОМПОНЕНТ ДЛЯ APP_ROUTE.
 * Он включает в себя Scaffold, BottomBar и NavHost для управления вкладками нижнего меню.
 * rootNavController передается, чтобы экраны вкладок могли переходить на Root-экраны (например, Notifications).
 */
@Composable
fun AppNavHost(rootNavController: NavHostController) {
    val bottomNavController = rememberNavController() // Локальный NavController для вкладок

    Scaffold(
        bottomBar = {
            BottomNavBar(bottomNavController)
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            // Стартовый маршрут — это маршрут содержимого первой вкладки
            startDestination = Screen.Main.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // 🔑 MainScreen теперь получает rootNavController, чтобы открыть NotificationsScreen
            composable(Screen.Main.route) {
                MainScreen(rootNavController = rootNavController)
            }
            // Все остальные вкладки используют свои уникальные маршруты
            composable(Screen.Object.route) { ObjectScreenStub() }
            composable(Screen.Map.route) { MapScreenStub() }
            composable(Screen.Docs.route) { DocsScreenStub() }
            composable(Screen.Profile.route) { ProfileScreenStub() }
        }
    }
}

/**
 * Компонент нижнего навигационного бара.
 */
@Composable
fun BottomNavBar(navController: NavHostController) {
    NavigationBar {
        // Убедимся, что мы правильно отслеживаем текущее состояние
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        bottomNavItems.forEach { tab ->
            val isSelected = currentRoute == tab.screen.route

            NavigationBarItem(
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) },
                selected = isSelected,
                onClick = {
                    navController.navigate(tab.screen.route) {
                        // Очистка стека до стартового маршрута для нижнего бара
                        popUpTo(navController.graph.findStartDestination().id) {
                            // Сохраняем состояние, чтобы переключиться между вкладками без потери данных
                            saveState = true
                        }
                        // Избегаем создания нескольких копий одного и того же destination
                        launchSingleTop = true
                        // Восстанавливаем предыдущее состояние при повторном выборе той же вкладки
                        restoreState = true
                    }
                }
            )
        }
    }
}

// УДАЛЕН AppNavHostWithBottomBar, так как его логика теперь в AppNavHost
// УДАЛЕНЫ неиспользуемые маршруты BottomBarWrapper и Auth из sealed class Screen