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
import com.example.kontrog.ui.screens.auth.AuthScreen // Экран аутентификации
import com.example.kontrog.ui.navigation.AppNavHost
import com.example.kontrog.ui.screens.auth.CodeVerificationScreen
import com.example.kontrog.ui.screens.auth.PhoneAuthViewModel
import com.example.kontrog.ui.theme.KontrogTheme

// Определяем корневые маршруты
object RootDestinations {
    const val AUTH_ROUTE = "auth_root"
    const val PIN_CODE_ROUTE = "pin_code_root"
    const val APP_ROUTE = "app_root"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KontrogTheme {
                val navController = rememberNavController()
                // 💡 ПРИМЕЧАНИЕ: Предполагается, что вы создали класс AuthViewModel
                val authViewModel: AuthViewModel = viewModel()
                val phoneAuthViewModel: PhoneAuthViewModel = viewModel()
                val authState by authViewModel.authState.collectAsState()

                // 💡 Определяем, с какого экрана начать
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
                    // --- 1. ЭКРАН АУТЕНТИФИКАЦИИ (Первичный вход) ---
                    composable(RootDestinations.AUTH_ROUTE) {
                        AuthScreen(
                            // 💡 Изменено: теперь ожидается, что AuthScreen вернет телефон.
                            // Смотри исправления в AuthScreen.kt и LoginScreen.kt
                            onAuthSuccess = { phoneNumber ->
                                phoneAuthViewModel.sendVerificationCode(phoneNumber, this@MainActivity)
                                navController.navigate(RootDestinations.PIN_CODE_ROUTE)
                            }
                        )
                    }

                    // --- 2. ЭКРАН ВВОДА ПИН-КОДА (2FA) ---
                    composable(RootDestinations.PIN_CODE_ROUTE) {
                        CodeVerificationScreen(
                            viewModel = phoneAuthViewModel,
                            onVerificationSuccess = {
                                // 💡 Успешная верификация кода. Переход на главный экран.
                                navController.popBackStack() // Удаляет PIN_CODE_ROUTE из стека
                                navController.navigate(RootDestinations.APP_ROUTE)
                            }
                        )
                    }

                    // --- 3. ГЛАВНЫЙ КОНТЕЙНЕР ПРИЛОЖЕНИЯ ---
                    composable(RootDestinations.APP_ROUTE) {
                        AppNavHost(rootNavController = navController)
                    }
                }
            }
        }
    }
}