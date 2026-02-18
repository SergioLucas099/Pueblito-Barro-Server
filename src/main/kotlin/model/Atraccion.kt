package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class Atraccion(
    val _id: String? = null,
    val nombre: String,
    val tiempoXpersona: Int,
    val turnoActual: String,
    val activa: Boolean
)
