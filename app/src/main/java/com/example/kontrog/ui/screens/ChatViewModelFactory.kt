package com.example.kontrog.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.kontrog.network.GigachatClient

class ChatViewModelFactory(private val authorizationKey: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(GigachatClient(authorizationKey)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
