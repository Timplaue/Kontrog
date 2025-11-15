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
import com.example.kontrog.ui.screens.AuthScreen // Экран аутентификации
import com.example.kontrog.ui.navigation.AppNavHost
import com.example.kontrog.ui.theme.KontrogTheme

// Определяем корневые маршруты
object RootDestinations {
    const val AUTH_ROUTE = "auth_root"
    // Один маршрут для всего основного приложения с нижним меню
    const val APP_ROUTE = "app_root"
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

                // 💡 Определяем, с какого экрана начать
                // Проверяем, есть ли пользователь (authState.isAuthenticated) или нет
                val startDestination = if (authState.isAuthenticated) {
                    RootDestinations.APP_ROUTE
                } else {
                    RootDestinations.AUTH_ROUTE
                }

                // 🔑 Главный корневой NavHost
                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // --- 1. ЭКРАН АУТЕНТИФИКАЦИИ ---
                    composable(RootDestinations.AUTH_ROUTE) {
                        AuthScreen(
                            viewModel = authViewModel,
                            onAuthSuccess = { role ->
                                // При успешной аутентификации переходим на главный экран
                                // (роль будет использоваться внутри AppNavHost, если нужно)
                                navController.popBackStack() // Удаляем AuthScreen из стека
                                navController.navigate(RootDestinations.APP_ROUTE)
                            }
                        )
                    }

                    // --- 2. ГЛАВНЫЙ КОНТЕЙНЕР ПРИЛОЖЕНИЯ (С Bottom Bar) ---
                    composable(RootDestinations.APP_ROUTE) {
                        // 🔑 Используем новый компонент, который содержит всю навигацию с Bottom Bar
                        AppNavHost(rootNavController = navController)
                    }
                }
            }
        }
    }
}