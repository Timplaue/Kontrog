package com.example.kontrog.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kontrog.AuthViewModel
import com.example.kontrog.ui.components.KontrogOutlinedTextField
import com.example.kontrog.ui.theme.KontrogRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    onRegistrationSuccess: (String) -> Unit
) {
    // Состояние для полей ввода
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Состояние для ошибок
    var passwordError by remember { mutableStateOf<String?>(null) }
    val authState by viewModel.authState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "СОЗДАНИЕ АККАУНТА",
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

            // Заголовок "РЕГИСТРАЦИЯ"
            Text(
                text = "РЕГИСТРАЦИЯ",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 32.dp, bottom = 32.dp)
            )

            // Поле Email
            KontrogOutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = "EMAIL (ЛОГИН)",
                keyboardType = KeyboardType.Email
            )
            Spacer(Modifier.height(24.dp))

            // Поле Номер телефона
            KontrogOutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = "НОМЕР ТЕЛЕФОНА",
                keyboardType = KeyboardType.Phone // 🔑 Клавиатура для номера
            )
            Spacer(Modifier.height(24.dp))

            // Поле Пароль
            KontrogOutlinedTextField(
                value = password,
                onValueChange = { password = it; passwordError = null },
                label = "ПАРОЛЬ",
                isPassword = true
            )
            Spacer(Modifier.height(24.dp))

            // Поле Повторите Пароль
            KontrogOutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; passwordError = null },
                label = "ПОВТОРИТЕ ПАРОЛЬ",
                isPassword = true
            )

            // Отображение общей ошибки, если есть
            if (authState.error != null) {
                Text(
                    text = authState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            // Отображение ошибки совпадения паролей
            if (passwordError != null) {
                Text(
                    text = passwordError!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    if (password != confirmPassword) {
                        passwordError = "Пароли не совпадают!"
                    } else if (email.isBlank() || phone.isBlank() || password.isBlank()) {
                        passwordError = "Заполните все поля!"
                    } else {
                        viewModel.register(email, password, phone)
                    }
                },
                enabled = !authState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp)
                    .defaultMinSize(minHeight = 56.dp), // ← Гарантированная высота!
                colors = ButtonDefaults.buttonColors(
                    containerColor = KontrogRed,
                    contentColor = Color.White
                )
            ) {
                if (authState.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp)  // ← уменьшенный размер индикатора
                    )
                } else {
                    Text(
                        text = "ЗАРЕГИСТРИРОВАТЬСЯ",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

    LaunchedEffect(authState.isAuthenticated) {
        if (authState.isAuthenticated) {
            onRegistrationSuccess(authState.role ?: "user")
        }
    }
}