package com.example.kontrog.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.*
import com.example.kontrog.ui.screens.MapScreen
import com.example.kontrog.ui.screens.ProfileScreen

/* ------------------------------ ROUTES ------------------------------ */

sealed class AppRoute(val route: String) {
    object Notifications : AppRoute("notifications_route")

    object Main : AppRoute("main_tab")
    object Object : AppRoute("object_tab")
    object Map : AppRoute("map_tab")
    object Docs : AppRoute("docs_tab")
    object Profile : AppRoute("profile_tab")
}

/* ------------------------- BOTTOM NAV TABS ------------------------- */

data class BottomTab(
    val route: String,
    val icon: ImageVector,
    val label: String
)

val bottomTabs = listOf(
    BottomTab(AppRoute.Main.route, Icons.Default.Home, "Главная"),
    BottomTab(AppRoute.Object.route, Icons.Default.LocationOn, "Объект"),
    BottomTab(AppRoute.Map.route, Icons.Default.Home, "Карта"),
    BottomTab(AppRoute.Docs.route, Icons.Default.Home, "Документы"),
    BottomTab(AppRoute.Profile.route, Icons.Default.Person, "Профиль")
)

/* ------------------------------ SCREENS ------------------------------ */

@Composable
fun StubScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun NotificationsScreen(navController: NavHostController) {
    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Экран Уведомлений", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(12.dp))
            Button(onClick = { navController.popBackStack() }) {
                Text("Назад")
            }
        }
    }
}

@Composable
fun MainScreen(rootNav: NavHostController) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Главный Экран", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(12.dp))
            Button(onClick = { rootNav.navigate(AppRoute.Notifications.route) }) {
                Text("Открыть Уведомления")
            }
        }
    }
}

/* ---------------------------- NAVIGATION ----------------------------- */

@Composable
fun AppNavHost(rootNavController: NavHostController) {
    val bottomNavController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavBar(bottomNavController) }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = AppRoute.Main.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppRoute.Main.route) { MainScreen(rootNavController) }
            composable(AppRoute.Object.route) { StubScreen("Объект") }
            composable(AppRoute.Map.route) { MapScreen(rootNavController) }
            composable(AppRoute.Docs.route) { StubScreen("Документы") }
            composable(AppRoute.Profile.route) { ProfileScreen(rootNavController) }
        }
    }
}

@Composable
fun BottomNavBar(navController: NavHostController) {
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        bottomTabs.forEach { tab ->
            val selected = currentDestination?.hierarchy?.any {
                it.route == tab.route
            } == true

            NavigationBarItem(
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) },
                selected = selected,
                onClick = {
                    navController.navigate(tab.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}