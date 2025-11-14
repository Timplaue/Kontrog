package com.example.kontrog.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kontrog.AuthViewModel
import com.example.kontrog.R

import com.example.kontrog.ui.theme.KontrogRed

// ----------------------------------------------------
// 1. Маршруты (AuthRoutes)
// ----------------------------------------------------
sealed class AuthRoutes {
    data object SELECTION : AuthRoutes()
    data object LOGIN : AuthRoutes()
    data object REGISTER : AuthRoutes()
}


// ----------------------------------------------------
// 2. Главный навигационный компонент (AuthScreen)
// ----------------------------------------------------
@Composable
fun AuthScreen(
    onAuthSuccess: (String) -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val authState by viewModel.authState.collectAsState()

    // ПЕРВЫЙ ПЕРЕХОД: Если уже аутентифицирован, сразу выходим
    if (authState.isAuthenticated && authState.role != null) {
        onAuthSuccess(authState.role!!)
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
            onLoginSuccess = onAuthSuccess
        )

        AuthRoutes.REGISTER -> RegistrationScreen(
            viewModel = viewModel,
            onBack = { currentScreen = AuthRoutes.SELECTION },
            onRegistrationSuccess = onAuthSuccess
        )
    }
}

// ----------------------------------------------------
// Composable: Выбор (Авторизация/Регистрация)
// ----------------------------------------------------
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
            // 🔑 Применяем цвет фона из темы
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

// ----------------------------------------------------
// 4. Composable: Экран входа (Login)
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    onRegisterClick: () -> Unit,
    onLoginSuccess: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authState by viewModel.authState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "ДАННЫЕ ДЛЯ ВХОДА",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))

            KontrogOutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = "ЛОГИН",
                keyboardType = KeyboardType.Email
            )
            Spacer(Modifier.height(24.dp))
            KontrogOutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = "ПАРОЛЬ",
                isPassword = true
            )

            Spacer(Modifier.weight(1f))

            Button(
                onClick = { viewModel.signIn(email, password) },
                enabled = !authState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = KontrogRed,
                    contentColor = Color.White
                )
            ) {
                if (authState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = "ПРОДОЛЖИТЬ",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
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

    LaunchedEffect(authState.isAuthenticated) {
        if (authState.isAuthenticated) {
            onLoginSuccess(authState.role ?: "user")
        }
    }
}


// ----------------------------------------------------
// 5. Компонент: Стилизованное поле ввода (TextField)
// ----------------------------------------------------
@Composable
fun KontrogOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.Gray
            )
        },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.background,
            unfocusedContainerColor = MaterialTheme.colorScheme.background,
            focusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            focusedTextColor = MaterialTheme.colorScheme.onBackground,
            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
            cursorColor = KontrogRed
        ),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}

// ----------------------------------------------------
// 6. Composable: Экран регистрации (ЗАГЛУШКА)
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    onRegistrationSuccess: (String) -> Unit // Новый параметр
) {
    // ⚠️ Это заглушка. Мы будем ее заполнять следующим шагом.
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("РЕГИСТРАЦИЯ", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Экран регистрации пока пуст!", color = Color.White)
            Spacer(Modifier.height(16.dp))
            Button(onClick = {
                // Временный вызов для проверки навигации
                onRegistrationSuccess("temp_user")
            }) {
                Text("Проверить успешную регистрацию")
            }
        }
    }
}