package com.example.kontrog.data.repository

import com.google.firebase.firestore.FirebaseFirestore

/**
 * Простой провайдер для получения экземпляра репозитория.
 * В более крупных проектах используйте Hilt/Koin.
 */
object RepositoryProvider {

    // Ленивая инициализация Firebase Firestore
    private val firestoreInstance: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    // Ленивая инициализация репозитория
    val fireSafetyRepository: FireSafetyRepository by lazy {
        FireSafetyRepositoryImpl(firestoreInstance)
    }
}