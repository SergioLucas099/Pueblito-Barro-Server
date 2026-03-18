package com.example.routes

import com.example.db.MongoDB
import com.example.db.MongoDB.configuracionCollection
import com.example.model.ApiResponse
import com.example.model.Atraccion
import com.example.model.CrearTurnoRequest
import com.example.model.CrearTurnosMultiplesRequest
import com.example.model.EstadoTurno
import com.example.model.ProgramarReinicioRequest
import com.example.model.ReinicioConfig
import com.example.model.Turno
import com.example.model.TurnoResumenResponse
import com.example.model.UpdateResponse
import com.example.service.TurnosService
import com.example.sheduler.TurnosSheduler
import com.example.websocket.TurnosSocketManager
import com.mongodb.client.model.Filters
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.litote.kmongo.eq
import org.litote.kmongo.descending
import org.litote.kmongo.setTo
import java.time.LocalDateTime
import org.bson.types.ObjectId

fun Route.turnosRoutes() {

    route("/turnos") {

        post("/multiple") {

            val request = call.receive<CrearTurnosMultiplesRequest>()

            if (request.atraccionesIds.isEmpty()) {
                return@post call.respond(
                    mapOf("error" to "Debe seleccionar al menos una atracción")
                )
            }

            if (request.cantidadPersonas <= 0) {
                return@post call.respond(
                    mapOf("error" to "Cantidad de personas inválida")
                )
            }

            val resumenTurnos = mutableListOf<TurnoResumenResponse>()

            for (atraccionId in request.atraccionesIds) {

                val atraccion = MongoDB.atracciones.findOneById(atraccionId)

                if (atraccion == null) {
                    continue
                }

                // Obtener último turno
                val ultimoTurno = MongoDB.turnos
                    .find(Turno::atraccionId eq atraccionId)
                    .sort(descending(Turno::numeroTurno))
                    .limit(1)
                    .toList()
                    .firstOrNull()

                val ultimoNumeroTurnos = ultimoTurno?.numeroTurno?.toIntOrNull() ?: 0
                val turnoActualAtraccion = atraccion.turnoActual.toIntOrNull() ?: 0

                val base = maxOf(ultimoNumeroTurnos, turnoActualAtraccion)
                val siguienteNumero = String.format("%04d", base + 1)

                val duracion = atraccion.tiempoXpersona * request.cantidadPersonas
                val tiempoEsperaActual = atraccion.tiempoAcumulado

                val nuevoTurno = Turno(
                    atraccionId = atraccionId,
                    nombreAtraccion = atraccion.nombre,
                    telefono = request.telefono,
                    numeroTurno = siguienteNumero,
                    numeroPersonas = request.cantidadPersonas,
                    tiempoEspera = tiempoEsperaActual,
                    duracion = duracion,
                    estado = EstadoTurno.ESPERA,
                    fecha = request.fecha
                )

                val insertResult = MongoDB.turnos.insertOne(nuevoTurno)

                val nuevoTiempoAcumulado = atraccion.tiempoAcumulado + duracion

                val updateResult = MongoDB.atracciones.updateOneById(
                    atraccionId,
                    org.litote.kmongo.set(
                        Atraccion::tiempoAcumulado setTo nuevoTiempoAcumulado,
                        Atraccion::turnoActual setTo siguienteNumero
                    )
                )

                resumenTurnos.add(
                    TurnoResumenResponse(
                        atraccionId = atraccionId,
                        nombreAtraccion = atraccion.nombre,
                        numeroTurno = siguienteNumero,
                        duracionSegundos = duracion,
                        tiempoEspera = tiempoEsperaActual,
                        turnoActualAnterior = atraccion.turnoActual
                    )
                )
            }
            TurnosSocketManager.broadcast("TURNOS_UPDATED")

            call.respond(resumenTurnos)
        }

        post("/preview") {

            val request = call.receive<CrearTurnosMultiplesRequest>()

            if (request.atraccionesIds.isEmpty()) {
                return@post call.respond(
                    mapOf("error" to "Debe seleccionar al menos una atracción")
                )
            }

            if (request.cantidadPersonas <= 0) {
                return@post call.respond(
                    mapOf("error" to "Cantidad de personas inválida")
                )
            }

            val resumenTurnos = mutableListOf<TurnoResumenResponse>()

            for (atraccionId in request.atraccionesIds) {

                val atraccion = MongoDB.atracciones.findOneById(atraccionId)
                    ?: continue

                // 🚫 Si la atracción no está activa, no permitir
                if (!atraccion.activa) {
                    continue
                }

                // Obtener último turno
                // Último turno en colección turnos
                val ultimoTurno = MongoDB.turnos
                    .find(Turno::atraccionId eq atraccionId)
                    .sort(descending(Turno::numeroTurno))
                    .limit(1)
                    .toList()
                    .firstOrNull()

                val ultimoNumeroTurnos = ultimoTurno?.numeroTurno?.toIntOrNull() ?: 0

                // Turno actual guardado en la atracción
                val turnoActualAtraccion = atraccion.turnoActual.toIntOrNull() ?: 0

                // Tomar el mayor de los dos
                val base = maxOf(ultimoNumeroTurnos, turnoActualAtraccion)

                val siguienteNumero = String.format("%04d", base + 1)

                val duracion = atraccion.tiempoXpersona * request.cantidadPersonas

                val tiempoEsperaActual = atraccion.tiempoAcumulado

                resumenTurnos.add(
                    TurnoResumenResponse(
                        atraccionId = atraccionId,
                        nombreAtraccion = atraccion.nombre,
                        numeroTurno = siguienteNumero,
                        duracionSegundos = duracion,
                        tiempoEspera = tiempoEsperaActual,
                        turnoActualAnterior = atraccion.turnoActual
                    )
                )
            }

            call.respond(resumenTurnos)
        }

        post("/reiniciar"){

            TurnosService.reiniciarTurnos()

            call.respond(
                ApiResponse(
                    success = true,
                    mensaje = "Turnos Reiniciados"
                )
            )
        }

        post("/programarReinicio") {

            println("Endpoint /turnos/programarReinicio llamado")

            val request = call.receive<ProgramarReinicioRequest>()

            configuracionCollection.updateOne(
                Filters.eq("_id", "reinicio_turnos"),
                Updates.combine(
                    Updates.set("hora", request.hora),
                    Updates.set("minuto", request.minuto)
                ),
                UpdateOptions().upsert(true)
            )

            TurnosSheduler.programarReinicioDiario(
                request.hora,
                request.minuto
            )

            call.respond(
                ApiResponse(
                    success = true,
                    mensaje = "Reinicio Programado"
                )
            )
        }

        get {
            val lista = MongoDB.turnos.find().toList()
            call.respond(lista)
        }

        get("/estado/{estado}") {
            val estadoParam = call.parameters["estado"]!!
            val estadoEnum = EstadoTurno.valueOf(estadoParam)
            val lista = MongoDB.turnos
                .find(Turno::estado eq estadoEnum)
                .toList()
            call.respond(lista)
        }

        get("/configuracionReinicio") {

            val config = configuracionCollection
                .findOne(Filters.eq("_id", "reinicio_turnos"))

            if (config != null) {

                call.respond(config)

            } else {

                call.respond(
                    ReinicioConfig(
                        _id = "reinicio_turnos",
                        hora = 0,
                        minuto = 0,
                    )
                )
            }
        }

        put("/{id}") {
            val id = call.parameters["id"]!!
            val actualizado = call.receive<Turno>()

            val resultado = MongoDB.turnos.updateOne(
                Filters.eq("_id", id),
                Updates.combine(
                    Updates.set("estado", actualizado.estado),
                    Updates.set("tiempoEspera", actualizado.tiempoEspera),
                    Updates.set("duracion", actualizado.duracion)
                )
            )

            println("MODIFICADOS: ${resultado.modifiedCount}")

            TurnosSocketManager.broadcast("TURNOS_UPDATED")

            call.respond(
                UpdateResponse(
                    success = true,
                    modificados = resultado.modifiedCount.toInt()
                )
            )
        }

        delete("/{id}") {
            val id = call.parameters["id"]!!
            MongoDB.turnos.deleteOneById(id)
            call.respond(mapOf("deleted" to true))
        }
    }
}