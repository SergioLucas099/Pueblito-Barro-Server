package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class Turno(
    val _id: String? = null,
    val atraccionId: String,
    val nombreAtraccion: String,
    val numeroTurno: String,
    val numeroPersonas: Int,
    val telefono: String,
    val tiempoEspera: Int = 0,
    val duracion: Int = 0,
    val estado: EstadoTurno = EstadoTurno.ESPERA,
    val fecha: String
)