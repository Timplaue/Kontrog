package com.example.kontrog.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.kontrog.ui.screens.*
import androidx.navigation.NavGraph.Companion.findStartDestination

/** ------------------------------ ROUTES ------------------------------ */
sealed class AppRoute(val route: String) {
    object Main : AppRoute("main_tab")
    object Object : AppRoute("object_tab")
    object Map : AppRoute("map_tab")
    object Docs : AppRoute("docs_tab")
    object Profile : AppRoute("profile_tab")
}

/** ------------------------- STUB SCREEN ------------------------- */
@Composable
fun StubScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
    }
}

/** ------------------------- BOTTOM NAV ------------------------- */
data class BottomTab(val route: String, val icon: ImageVector, val label: String)

val bottomTabs = listOf(
    BottomTab(AppRoute.Main.route, Icons.Default.Home, "Главная"),
    BottomTab(AppRoute.Object.route, Icons.Default.LocationOn, "Объект"),
    BottomTab(AppRoute.Map.route, Icons.Default.Map, "Карта"),
    BottomTab(AppRoute.Docs.route, Icons.Default.Description, "Документы"),
    BottomTab(AppRoute.Profile.route, Icons.Default.Person, "Профиль")
)

/** ------------------------- NAV HOST ------------------------- */
@Composable
fun AppNavHost(rootNavController: NavHostController) {
    val bottomNavController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavBar(bottomNavController) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = AppRoute.Main.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppRoute.Main.route) { MainScreen(bottomNavController) }
            composable(AppRoute.Object.route) { ObjectScreen(bottomNavController) }
            composable(AppRoute.Map.route) { MapScreen(bottomNavController) }
            composable(AppRoute.Docs.route) { StubScreen("Документы") }
            composable(AppRoute.Profile.route) { ProfileScreen(bottomNavController) }
            composable("notifications") { NotificationsScreen(bottomNavController) }
        }
    }
}

/** ------------------------- BOTTOM NAV BAR ------------------------- */
@Composable
fun BottomNavBar(navController: NavHostController) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        bottomTabs.forEach { tab ->
            val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true

            NavigationBarItem(
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) },
                selected = selected,
                onClick = {
                    navController.navigate(tab.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
