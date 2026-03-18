package com.example.service

import com.example.db.MongoDB
import com.mongodb.client.model.Updates
import org.bson.Document

object TurnosService {

    suspend fun reiniciarTurnos(){

        println("Iniciando reinicio de turnos...")

        val update = Updates.combine(
            Updates.set("tiempoAcumulado", 0),
            Updates.set("turnoActual", "0000")
        )

        // Reiniciar atracciones
        MongoDB.atracciones.updateMany(
            Document(),
            update
        )

        // Eliminar todos los turnos
        MongoDB.turnos.deleteMany(Document())

        println("Turnos eliminados y atracciones reiniciadas correctamente")
    }
}