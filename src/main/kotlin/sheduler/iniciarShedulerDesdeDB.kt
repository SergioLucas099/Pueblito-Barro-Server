package com.example.sheduler

import com.example.db.MongoDB.configuracionCollection
import com.mongodb.client.model.Filters

suspend fun iniciarSchedulerDesdeDB() {

    val config = configuracionCollection
        .findOne(Filters.eq("_id", "reinicio_turnos"))

    if (config != null) {

        println("Configuracion encontrada ${config.hora}:${config.minuto}")

        TurnosSheduler.programarReinicioDiario(
            config.hora,
            config.minuto
        )

    } else {

        println("No hay configuracion de reinicio automatico")

    }
}