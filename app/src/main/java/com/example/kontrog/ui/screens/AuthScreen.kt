package com.example.kontrog.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kontrog.ui.theme.KontrogRed

sealed class AuthRoutes {
    data object SELECTION : AuthRoutes()
    data object LOGIN : AuthRoutes()
    data object REGISTER : AuthRoutes()
}

@Composable
fun AuthScreen(
    onAuthSuccess: (String) -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val authState by viewModel.authState.collectAsState()

    // ПРОВЕРКА АУТЕНТИФИКАЦИИ
    if (authState.isAuthenticated && authState.role != null) {
        onAuthSuccess(authState.role!!)
        return
    }

    // УПРАВЛЕНИЕ ТЕКУЩИМ ЭКРАНОМ
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
            onLoginSuccess = onAuthSuccess
        )

        AuthRoutes.REGISTER -> RegistrationScreen(
            viewModel = viewModel,
            onBack = { currentScreen = AuthRoutes.SELECTION },
            onLoginClick = { currentScreen = AuthRoutes.LOGIN },
            onRegistrationSuccess = onAuthSuccess
        )
    }
}


@Composable
fun SelectionScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 1. Используем Column для вертикального выравнивания всех элементов
    Column(
        modifier = modifier
            .fillMaxSize()
            // Применяем цвет фона из темы
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 2. Верхний заголовок "ВОЙТИ В КОНТР.ОРГ"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, bottom = 120.dp), // Отступы сверху
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ВОЙТИ В КОНТР.",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "ОГ",
                color = KontrogRed, // 🔑 Акцентный цвет
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // 3. Изображение (пламя)
        // ⚠️ Предполагается, что вы добавили изображение пламени в папку res/drawable
        // Назовем его, например, 'ic_flame'
        Image(
            painter = painterResource(id = R.drawable.ic_flame),
            contentDescription = "Логотип пламени",
            modifier = Modifier
                .size(180.dp)
                .weight(1f) // 🔑 Занимает все оставшееся вертикальное пространство
        )

        // 4. Кнопка "ВОЙТИ В АККАУНТ" (Login)
        Button(
            onClick = onLoginClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(bottom = 16.dp), // Отступ между кнопкой и текстом
            colors = ButtonDefaults.buttonColors(
                containerColor = KontrogRed, // 🔑 Красный фон кнопки
                contentColor = Color.White // Белый текст
            )
        ) {
            Text(
                text = "ВОЙТИ В АККАУНТ",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // 5. Текст "ПЕРВЫЙ ВХОД?" (Register)
        TextButton(
            onClick = onRegisterClick,
            modifier = Modifier.padding(bottom = 40.dp) // Отступ снизу
        ) {
            Text(
                text = "ПЕРВЫЙ ВХОД?",
                color = Color.White.copy(alpha = 0.7f), // Полупрозрачный белый
                fontSize = 14.sp
            )
        }
    }
}