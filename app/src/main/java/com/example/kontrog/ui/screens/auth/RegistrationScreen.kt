// RegistrationScreen.kt
package com.example.kontrog.ui.screens.auth

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
    onLoginClick: () -> Unit,
    onRegistrationSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var position by remember { mutableStateOf("") }

    var validationError by remember { mutableStateOf<String?>(null) }
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

            KontrogOutlinedTextField(
                value = email,
                onValueChange = { email = it; validationError = null },
                label = "EMAIL",
                keyboardType = KeyboardType.Email
            )
            Spacer(Modifier.height(16.dp))

            KontrogOutlinedTextField(
                value = phone,
                onValueChange = { phone = it; validationError = null },
                label = "НОМЕР ТЕЛЕФОНА",
                keyboardType = KeyboardType.Phone
            )
            Spacer(Modifier.height(16.dp))

            KontrogOutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = "ПОЛНОЕ ИМЯ (необязательно)",
                keyboardType = KeyboardType.Text
            )
            Spacer(Modifier.height(16.dp))

            KontrogOutlinedTextField(
                value = position,
                onValueChange = { position = it },
                label = "ДОЛЖНОСТЬ (необязательно)",
                keyboardType = KeyboardType.Text
            )
            Spacer(Modifier.height(16.dp))

            KontrogOutlinedTextField(
                value = password,
                onValueChange = { password = it; validationError = null },
                label = "ПАРОЛЬ",
                isPassword = true
            )
            Spacer(Modifier.height(16.dp))

            KontrogOutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; validationError = null },
                label = "ПОВТОРИТЕ ПАРОЛЬ",
                isPassword = true
            )

            // Ошибки
            if (authState.error != null) {
                Text(
                    text = authState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            if (validationError != null) {
                Text(
                    text = validationError!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    when {
                        email.isBlank() || phone.isBlank() || password.isBlank() -> {
                            validationError = "Заполните обязательные поля!"
                        }
                        password != confirmPassword -> {
                            validationError = "Пароли не совпадают!"
                        }
                        password.length < 6 -> {
                            validationError = "Пароль должен содержать минимум 6 символов"
                        }
                        else -> {
                            viewModel.register(
                                email = email,
                                password = password,
                                phone = phone,
                                fullName = fullName,
                                position = position
                            )
                        }
                    }
                },
                enabled = !authState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .defaultMinSize(minHeight = 56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = KontrogRed,
                    contentColor = Color.White
                )
            ) {
                if (authState.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = "СОЗДАТЬ АККАУНТ",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            TextButton(
                onClick = onLoginClick,
                modifier = Modifier.padding(bottom = 40.dp)
            ) {
                Text(
                    text = "УЖЕ ЕСТЬ АККАУНТ?",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }
        }
    }

    // Переход после успешной регистрации
    LaunchedEffect(authState.isAuthenticated) {
        if (authState.isAuthenticated && authState.user != null) {
            onRegistrationSuccess()
        }
    }
}