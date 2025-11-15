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
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kontrog.AuthViewModel
import com.example.kontrog.ui.components.KontrogOutlinedTextField
import com.example.kontrog.ui.theme.KontrogRed
import android.util.Log

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
            val phone = viewModel.authState.value.phoneNumber

            if (phone != null) {
                onLoginSuccess(phone)
            } else {
                Log.e("LoginScreen", "User authenticated but phone number is null.")
            }
        }
    }
}