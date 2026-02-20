package com.example.model

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId

@Serializable
data class Multimedia(
    @BsonId
    val _id: String? = null,
    val tipo: String,
    val url: String,
    val activo: Boolean = true,
    val sonido: Boolean = false,
    val nombre: String
)