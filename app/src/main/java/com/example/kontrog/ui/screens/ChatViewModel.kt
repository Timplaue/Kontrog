package com.example.kontrog.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kontrog.network.GigachatClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel(private val client: GigachatClient) : ViewModel() {

    private val _messages = MutableStateFlow<List<String>>(emptyList())
    val messages: StateFlow<List<String>> = _messages

    fun sendMessage(userMessage: String) {
        _messages.value = _messages.value + "Вы: $userMessage"
        viewModelScope.launch {
            try {
                val response = client.sendMessage(userMessage)
                _messages.value = _messages.value + "AI: $response"
            } catch (e: Exception) {
                _messages.value = _messages.value + "Ошибка: ${e.message}"
            }
        }
    }
}
