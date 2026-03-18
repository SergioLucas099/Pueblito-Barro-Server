package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class ProgramarReinicioRequest (
    val hora: Int,
    val minuto: Int
)