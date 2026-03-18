package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class ReinicioConfig (
    val _id: String = "reinicio_turnos",
    val hora: Int,
    val minuto: Int,
)