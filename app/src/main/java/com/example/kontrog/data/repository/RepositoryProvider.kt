package com.example.kontrog.data.repository

import com.google.firebase.firestore.FirebaseFirestore

object RepositoryProvider {

    private val firestoreInstance: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    val fireSafetyRepository: FireSafetyRepository by lazy {
        FireSafetyRepositoryImpl(firestoreInstance)
    }
}