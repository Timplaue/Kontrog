// MainActivity.kt
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
import com.example.kontrog.ui.screens.auth.AuthScreen
import com.example.kontrog.ui.navigation.AppNavHost
import com.example.kontrog.ui.screens.auth.CodeVerificationScreen
import com.example.kontrog.ui.screens.auth.PhoneAuthViewModel
import com.example.kontrog.ui.theme.KontrogTheme

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
                val authViewModel: AuthViewModel = viewModel()
                val phoneAuthViewModel: PhoneAuthViewModel = viewModel()
                val authState by authViewModel.authState.collectAsState()

                // Определяем стартовый экран на основе статуса аутентификации
                val startDestination = when {
                    authState.isLoading -> RootDestinations.AUTH_ROUTE
                    authState.isAuthenticated && authState.needsPhoneVerification -> {
                        RootDestinations.PIN_CODE_ROUTE
                    }
                    authState.isAuthenticated -> {
                        RootDestinations.APP_ROUTE
                    }
                    else -> RootDestinations.AUTH_ROUTE
                }

                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 1. ЭКРАН АУТЕНТИФИКАЦИИ (Email/Password)
                    composable(RootDestinations.AUTH_ROUTE) {
                        AuthScreen(
                            onAuthSuccess = {
                                // После успешной авторизации проверяем нужна ли верификация телефона
                                val currentState = authViewModel.authState.value
                                if (currentState.needsPhoneVerification) {
                                    // Отправляем код на телефон
                                    val phoneNumber = authViewModel.getPhoneNumber()
                                    if (phoneNumber != null) {
                                        phoneAuthViewModel.sendVerificationCode(
                                            phoneNumber,
                                            this@MainActivity
                                        )
                                        navController.navigate(RootDestinations.PIN_CODE_ROUTE) {
                                            popUpTo(RootDestinations.AUTH_ROUTE) { inclusive = true }
                                        }
                                    }
                                } else {
                                    // Телефон уже верифицирован, идём в приложение
                                    navController.navigate(RootDestinations.APP_ROUTE) {
                                        popUpTo(RootDestinations.AUTH_ROUTE) { inclusive = true }
                                    }
                                }
                            },
                            viewModel = authViewModel
                        )
                    }

                    // 2. ЭКРАН ВЕРИФИКАЦИИ ТЕЛЕФОНА (2FA)
                    composable(RootDestinations.PIN_CODE_ROUTE) {
                        CodeVerificationScreen(
                            viewModel = phoneAuthViewModel,
                            onVerificationSuccess = {
                                // Помечаем телефон как верифицированный в базе
                                authViewModel.markPhoneVerified()

                                // Переходим в приложение
                                navController.navigate(RootDestinations.APP_ROUTE) {
                                    popUpTo(RootDestinations.AUTH_ROUTE) { inclusive = true }
                                    popUpTo(RootDestinations.PIN_CODE_ROUTE) { inclusive = true }
                                }
                            }
                        )
                    }

                    // 3. ГЛАВНОЕ ПРИЛОЖЕНИЕ
                    composable(RootDestinations.APP_ROUTE) {
                        AppNavHost(rootNavController = navController)
                    }
                }
            }
        }
    }
}