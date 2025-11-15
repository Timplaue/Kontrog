package com.example.kontrog.data.models

import com.google.firebase.firestore.DocumentId

data class Person(
    @DocumentId
    val id: String = "",
    val organizationId: String = "",
    val userId: String? = null,

    val fullName: String = "",
    val position: String = "",

    val phone: String = "",
    val email: String = "",

    val responsibilityType: String = ""
)