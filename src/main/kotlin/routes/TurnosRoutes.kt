package com.example.routes

import com.example.db.MongoDB
import com.example.model.CrearTurnoRequest
import com.example.model.EstadoTurno
import com.example.model.Turno
import com.example.websocket.TurnosSocketManager
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.litote.kmongo.eq
import org.litote.kmongo.descending
import java.time.LocalDateTime

fun Route.turnosRoutes() {

    route("/turnos") {

        post {

            val request = call.receive<CrearTurnoRequest>()

            val ultimoTurno = MongoDB.turnos
                .find(Turno::atraccionId eq request.atraccionId)
                .sort(descending(Turno::numeroTurno))
                .limit(1)
                .toList()
                .firstOrNull()

            val siguienteNumero = if (ultimoTurno == null) {
                "0001"
            } else {
                val numeroActual = ultimoTurno.numeroTurno.toInt()
                String.format("%04d", numeroActual + 1)
            }

            val nuevoTurno = Turno(
                atraccionId = request.atraccionId,
                nombre = request.nombre,
                telefono = request.telefono,
                numeroTurno = siguienteNumero,
                tiempoEspera = 0,
                estado = EstadoTurno.ESPERA,
                fecha = request.fecha
            )

            MongoDB.turnos.insertOne(nuevoTurno)

            TurnosSocketManager.broadcast("TURNOS_UPDATED")

            call.respond(nuevoTurno)
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

        put("/{id}") {
            val id = call.parameters["id"]!!
            val actualizado = call.receive<Turno>()
            MongoDB.turnos.updateOneById(id, actualizado)
            call.respond(mapOf("success" to true))
        }

        delete("/{id}") {
            val id = call.parameters["id"]!!
            MongoDB.turnos.deleteOneById(id)
            call.respond(mapOf("deleted" to true))
        }
    }
}