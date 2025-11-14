package com.example.kontrog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kontrog.ui.screens.AuthScreen
import com.example.kontrog.ui.screens.AdminScreen
import com.example.kontrog.ui.screens.HomeScreen
import com.example.kontrog.ui.theme.KontrogTheme

// Определяем основные маршруты
object Destinations {
    const val AUTH_ROUTE = "auth"
    const val HOME_ROUTE = "home"
    const val ADMIN_ROUTE = "admin"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KontrogTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel()
                val authState by authViewModel.authState.collectAsState()

                // 🔑 Главный навигатор
                NavHost(
                    navController = navController,
                    startDestination = Destinations.AUTH_ROUTE, // Начинаем с экрана аутентификации
                    modifier = Modifier.fillMaxSize()
                ) {

                    // --- 1. ЭКРАН АУТЕНТИФИКАЦИИ ---
                    composable(Destinations.AUTH_ROUTE) {
                        // Используем AuthScreen как стартовую точку
                        AuthScreen(
                            viewModel = authViewModel,
                            onAuthSuccess = { role ->
                                // При успешной аутентификации переходим на главный экран в зависимости от роли
                                navController.popBackStack() // Удаляем AuthScreen из стека
                                if (role == "admin") {
                                    navController.navigate(Destinations.ADMIN_ROUTE)
                                } else {
                                    navController.navigate(Destinations.HOME_ROUTE)
                                }
                            }
                        )
                    }

                    // --- 2. ГЛАВНЫЙ ЭКРАН (USER) ---
                    composable(Destinations.HOME_ROUTE) {
                        // Передаем роль на экран
                        HomeScreen(role = authState.role ?: "user")
                    }

                    // --- 3. ГЛАВНЫЙ ЭКРАН (ADMIN) ---
                    composable(Destinations.ADMIN_ROUTE) {
                        // Передаем роль на экран
                        AdminScreen(role = authState.role ?: "admin")
                    }
                }
            }
        }
    }
}