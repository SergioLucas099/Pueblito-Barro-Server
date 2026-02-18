package com.example.model

import kotlinx.serialization.Serializable

@Serializable
enum class EstadoTurno {
    EN_ESPERA,
    APROBADO,
    LLAMADO,
    FINALIZADO,
    CANCELADO,
}