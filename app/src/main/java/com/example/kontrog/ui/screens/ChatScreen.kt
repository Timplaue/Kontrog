package com.example.kontrog.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@Composable
fun ChatScreen(
    navController: NavController,
    authorizationKey: String,
    chatViewModel: ChatViewModel = viewModel(factory = ChatViewModelFactory(authorizationKey))
) {
    var input by remember { mutableStateOf("") }
    val messages by chatViewModel.messages.collectAsState()
    @OptIn(ExperimentalMaterial3Api::class)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Чат с AI") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f).padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(messages) { msg -> Text(msg) }
            }

            Row(modifier = Modifier.padding(8.dp)) {
                TextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Введите сообщение") }
                )
                Button(onClick = {
                    if (input.isNotBlank()) {
                        chatViewModel.sendMessage(input)
                        input = ""
                    }
                }) {
                    Text("Отправить")
                }
            }
        }
    }
}
