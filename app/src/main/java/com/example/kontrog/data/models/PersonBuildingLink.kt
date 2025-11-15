package com.example.kontrog.data.models

import com.google.firebase.firestore.DocumentId

data class PersonBuildingLink(
    @DocumentId
    val id: String = "",

    val personId: String = "",

    val buildingId: String = "",

    val responsibilityDetail: String = ""
)