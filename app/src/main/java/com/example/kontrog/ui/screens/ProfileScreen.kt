package com.example.kontrog.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.kontrog.ProfileViewModel
import com.example.kontrog.RootDestinations

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    rootNavController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {
    val scrollState = rememberScrollState()
    val profileState by viewModel.profileState.collectAsState()

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White
                ),
                title = { Text("ПРОФИЛЬ", fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->

        if (profileState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
            return@Scaffold
        }

        if (profileState.error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = profileState.error!!,
                        color = Color.Red,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadProfileData() }) {
                        Text("Повторить")
                    }
                }
            }
            return@Scaffold
        }

        val data = profileState.data

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color.Black)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileHeader(
                fullName = data.fullName,
                position = data.position,
                organization = data.organization,
                onAvatarChange = { /* TODO: Реализовать загрузку аватара */ }
            )
            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader(title = "ДАННЫЕ")
            ProfileInfoRow(label = "ЭЛ.ПОЧТА", value = data.email)
            ProfileInfoRow(label = "ТЕЛЕФОН", value = data.phone)
            ProfileInfoRow(
                label = "ТИП ОТВЕТСТВЕННОСТИ",
                value = data.responsibilityType.ifEmpty { "Не указан" },
                isLast = true
            )
            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader(title = "СТАТИСТИКА")
            ProfileStatRow(
                label = "ПРИВЯЗАННЫЕ ОБЪЕКТЫ",
                value = data.attachedObjects.toString(),
                onClick = { /* TODO */ }
            )
            ProfileStatRow(
                label = "ВЫПОЛНЕНО ПРОВЕРОК",
                value = data.completedChecks.toString(),
                onClick = { /* TODO */ }
            )
            ProfileStatRow(
                label = "ПРОСРОЧЕННЫХ СРЕДСТВ",
                value = data.overdueFunds.toString(),
                isLast = true,
                onClick = { /* TODO */ }
            )
            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader(title = "НАСТРОЙКИ")
            ProfileNavRow(label = "ВЫБОР КАРТЫ: YANDEX", onClick = { /* TODO */ })
            ProfileNavRow(label = "ОФЛАЙН-СИНХРОНИЗАЦИЯ: ВКЛ", onClick = { /* TODO */ })
            ProfileNavRow(label = "СМЕНИТЬ PIN", onClick = { /* TODO */ })

            ProfileNavRow(
                label = "ВЫЙТИ",
                isLogout = true,
                onClick = {
                    viewModel.signOut()
                    rootNavController.navigate(RootDestinations.AUTH_ROUTE) {
                        popUpTo(rootNavController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                    }
                }
            )

            // --- 6. Версия ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "1.1 (20013)",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun ProfileHeader(
    fullName: String,
    position: String,
    organization: String,
    onAvatarChange: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = fullName.firstOrNull()?.toString() ?: "?",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = fullName.uppercase(),
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = buildString {
                append(position.uppercase())
                if (organization.isNotEmpty()) {
                    append("\n${organization.uppercase()}")
                }
            },
            color = Color.Gray,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
        TextButton(onClick = onAvatarChange) {
            Text(
                "ИЗМЕНИТЬ АВАТАР",
                color = Color(0xFF67B5FF),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        color = Color.Gray,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
}

@Composable
fun ProfileInfoRow(label: String, value: String, isLast: Boolean = false) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = Color.White, style = MaterialTheme.typography.bodyMedium)
            Text(value, color = Color.White, style = MaterialTheme.typography.bodyMedium)
        }
        if (!isLast) {
            HorizontalDivider(color = Color(0xFF2E2E2E), thickness = 1.dp)
        }
    }
}

@Composable
fun ProfileStatRow(label: String, value: String, isLast: Boolean = false, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = Color.White, style = MaterialTheme.typography.bodyMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(value, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                Icon(Icons.Default.ChevronRight, contentDescription = "Далее", tint = Color.Gray)
            }
        }
        if (!isLast) {
            HorizontalDivider(color = Color(0xFF2E2E2E), thickness = 1.dp)
        }
    }
}

@Composable
fun ProfileNavRow(label: String, isLogout: Boolean = false, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                color = if (isLogout) Color(0xFF67B5FF) else Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
            if (!isLogout) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Далее", tint = Color.Gray)
            }
        }
        HorizontalDivider(color = Color(0xFF2E2E2E), thickness = 1.dp)
    }
}