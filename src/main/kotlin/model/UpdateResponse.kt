package com.example.model

@kotlinx.serialization.Serializable
data class UpdateResponse(
    val success: Boolean,
    val modificados: Int
)