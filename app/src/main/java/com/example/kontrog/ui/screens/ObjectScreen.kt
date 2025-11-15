package com.example.kontrog.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.kontrog.data.models.Building

/**
 * 🏢 Экран Реестра Объектов (Зданий).
 * Отображает список зданий, загруженных из Firestore.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObjectScreen(
    rootNavController: NavController,
    viewModel: ObjectViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("РЕЕСТР ОБЪЕКТОВ", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* TODO: Навигация на экран добавления объекта */ }) {
                Icon(Icons.Filled.Add, contentDescription = "Добавить объект")
            }
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when (val state = uiState) {
                ObjectListUiState.Loading -> {
                    // Индикатор загрузки
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ObjectListUiState.Error -> {
                    // Сообщение об ошибке
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                is ObjectListUiState.Success -> {
                    if (state.buildings.isEmpty()) {
                        // Пустое состояние
                        Text(
                            text = "Объекты не найдены. Добавьте первый объект!",
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        // Отображение списка объектов
                        ObjectList(buildings = state.buildings)
                    }
                }
            }
        }
    }
}

@Composable
fun ObjectList(buildings: List<Building>) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(buildings) { building ->
            ObjectItem(building = building)
        }
    }
}

@Composable
fun ObjectItem(building: Building) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 1. Адрес и Тип
            Text(
                text = building.address,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Тип: ${building.type} | Этажей: ${building.floors}",
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall
            )

            // 2. Координаты (для отладки)
            Text(
                text = "Координаты: L:${building.latitude.format(4)}, G:${building.longitude.format(4)}",
                color = Color.Gray,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

// Вспомогательная функция для форматирования Double (для отладки)
fun Double.format(digits: Int) = "%.${digits}f".format(this)