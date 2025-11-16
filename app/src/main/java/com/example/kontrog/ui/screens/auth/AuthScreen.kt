package com.example.kontrog.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kontrog.AuthViewModel
import com.example.kontrog.R
import com.example.kontrog.ui.theme.KontrogRed

sealed class AuthRoutes {
    object SELECTION : AuthRoutes()
    object LOGIN : AuthRoutes()
    object REGISTER : AuthRoutes()
}

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val authState by viewModel.authState.collectAsState()

    if (authState.isAuthenticated && authState.user != null) {
        onAuthSuccess()
        return
    }

    var currentScreen by remember { mutableStateOf<AuthRoutes>(AuthRoutes.SELECTION) }

    when (currentScreen) {
        AuthRoutes.SELECTION -> SelectionScreen(
            onLoginClick = { currentScreen = AuthRoutes.LOGIN },
            onRegisterClick = { currentScreen = AuthRoutes.REGISTER }
        )
        AuthRoutes.LOGIN -> LoginScreen(
            viewModel = viewModel,
            onBack = { currentScreen = AuthRoutes.SELECTION },
            onRegisterClick = { currentScreen = AuthRoutes.REGISTER },
            onLoginSuccess = {
                onAuthSuccess()
            }
        )
        AuthRoutes.REGISTER -> RegistrationScreen(
            viewModel = viewModel,
            onBack = { currentScreen = AuthRoutes.SELECTION },
            onLoginClick = { currentScreen = AuthRoutes.LOGIN },
            onRegistrationSuccess = {
                onAuthSuccess()
            }
        )
    }
}

@Composable
fun SelectionScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, bottom = 120.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ВОЙТИ В КОНТР.",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "ОРГ",
                color = KontrogRed,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Image(
            painter = painterResource(id = R.drawable.ic_flame),
            contentDescription = "Логотип пламени",
            modifier = Modifier
                .size(180.dp)
                .weight(1f)
        )

        Button(
            onClick = onLoginClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = KontrogRed,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "ВОЙТИ В АККАУНТ",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        TextButton(
            onClick = onRegisterClick,
            modifier = Modifier.padding(bottom = 40.dp)
        ) {
            Text(
                text = "ПЕРВЫЙ ВХОД?",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        }
    }
}