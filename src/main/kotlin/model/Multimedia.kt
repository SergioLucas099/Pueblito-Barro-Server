package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class Multimedia(
    val _id: String? = null,
    val tipo: String, // VIDEO, IMAGEN
    val url: String,
    val activo: Boolean = true
)
