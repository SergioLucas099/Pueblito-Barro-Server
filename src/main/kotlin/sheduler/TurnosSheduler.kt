package com.example.sheduler

import com.example.service.TurnosService.reiniciarTurnos
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import kotlin.time.Duration

object TurnosSheduler {

    private var job: Job? = null

    fun iniciar(hora: Int, minuto: Int) {

        job?.cancel()

        job = GlobalScope.launch {

            while (true) {

                val ahora = LocalDateTime.now()

                val proximoReinicio = ahora
                    .withHour(hora)
                    .withMinute(minuto)
                    .withSecond(0)

                val tiempoEspera =
                    java.time.Duration.between(
                        ahora,
                        if (proximoReinicio.isAfter(ahora))
                            proximoReinicio
                        else
                            proximoReinicio.plusDays(1)
                    ).toMillis()

                println("Proximo reinicio en $tiempoEspera")

                delay(tiempoEspera)

                reiniciarTurnos()
            }
        }
    }

    fun programarReinicioDiario(
        hora: Int,
        minuto: Int
    ) {
        iniciar(hora, minuto)

        println("Reinicio programado $hora:$minuto")
    }
}