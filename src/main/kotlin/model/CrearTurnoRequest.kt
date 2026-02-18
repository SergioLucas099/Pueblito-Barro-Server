package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class CrearTurnoRequest(
    val atraccionId: String,
    val nombre: String,
    val telefono: String,
    val fecha: String
)
