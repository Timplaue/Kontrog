package com.example.kontrog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kontrog.ui.screens.auth.AuthScreen
import com.example.kontrog.ui.screens.auth.CodeVerificationScreen
import com.example.kontrog.ui.screens.auth.PhoneAuthViewModel
import com.example.kontrog.ui.theme.KontrogTheme
import com.example.kontrog.ui.navigation.AppNavHost

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
                val rootNavController = rememberNavController()

                val authViewModel: AuthViewModel = viewModel()
                val phoneAuthViewModel: PhoneAuthViewModel = viewModel()
                val authState by authViewModel.authState.collectAsState()
                val startDestination = RootDestinations.AUTH_ROUTE

                NavHost(
                    navController = rootNavController,
                    startDestination = startDestination
                ) {

                    // ------------------- AUTH -------------------
                    composable(RootDestinations.AUTH_ROUTE) {
                        AuthScreen(
                            viewModel = authViewModel,
                            onAuthSuccess = {
                                val state = authViewModel.authState.value
                                if (state.needsPhoneVerification) {
                                    val phone = authViewModel.getPhoneNumber()
                                    if (phone != null) {
                                        phoneAuthViewModel.sendVerificationCode(
                                            phone,
                                            this@MainActivity
                                        )
                                    }
                                    rootNavController.navigate(RootDestinations.PIN_CODE_ROUTE) {
                                        popUpTo(RootDestinations.AUTH_ROUTE) { inclusive = true }
                                    }
                                } else {
                                    rootNavController.navigate(RootDestinations.APP_ROUTE) {
                                        popUpTo(RootDestinations.AUTH_ROUTE) { inclusive = true }
                                    }
                                }
                            }
                        )
                    }

                    // ------------------- PIN CODE -------------------
                    composable(RootDestinations.PIN_CODE_ROUTE) {
                        CodeVerificationScreen(
                            viewModel = phoneAuthViewModel,
                            onVerificationSuccess = {
                                authViewModel.markPhoneVerified()
                                rootNavController.navigate(RootDestinations.APP_ROUTE) {
                                    popUpTo(RootDestinations.AUTH_ROUTE) { inclusive = true }
                                    popUpTo(RootDestinations.PIN_CODE_ROUTE) { inclusive = true }
                                }
                            }
                        )
                    }

                    // ------------------- APP -------------------
                    composable(RootDestinations.APP_ROUTE) {
                        AppNavHost(rootNavController)
                    }
                }
            }
        }
    }
}
