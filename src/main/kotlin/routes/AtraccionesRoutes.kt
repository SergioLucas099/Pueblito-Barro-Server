package com.example.routes

import com.example.db.MongoDB
import com.example.model.Atraccion
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.litote.kmongo.eq
import org.litote.kmongo.setTo
import com.example.websocket.AtraccionesSocketManager
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun Route.atraccionesRoutes() {

    route("/atracciones") {

        // Crear atracción
        post {
            val atraccion = call.receive<Atraccion>()

            if (atraccion.nombre.isBlank()) {
                return@post call.respond(
                    mapOf("error" to "El nombre no puede estar vacío")
                )
            }

            if (atraccion.tiempoXpersona <= 0) {
                return@post call.respond(
                    mapOf("error" to "El tiempo por persona no puede estar en 00:00")
                )
            }

            MongoDB.atracciones.insertOne(atraccion)

            // Broadcast en tiempo real
            AtraccionesSocketManager.broadcast(
                Json.encodeToString(atraccion)
            )

            call.respond(atraccion)
        }

        // Listar todas
        get {
            val lista = MongoDB.atracciones.find().toList()
            call.respond(lista)
        }

        // Listar solo activas (IMPORTANTE para TurnosCrear)
        get("/activas") {
            val lista = MongoDB.atracciones
                .find(Atraccion::activa eq true)
                .toList()

            call.respond(lista)
        }

        // Actualizar
        put("/{id}") {
            val id = call.parameters["id"]!!
            val actualizada = call.receive<Atraccion>()

            MongoDB.atracciones.updateOneById(id, actualizada)

            call.respond(mapOf("success" to true))
        }

        // Activar / Desactivar
        patch("/{id}/estado") {
            val id = call.parameters["id"]!!
            val body = call.receive<Map<String, Boolean>>()
            val activa = body["activa"] ?: true

            MongoDB.atracciones.updateOneById(
                id,
                Atraccion::activa setTo activa
            )

            call.respond(mapOf("activa" to activa))
        }

        delete("/{id}") {
            val id = call.parameters["id"]!!
            MongoDB.atracciones.deleteOneById(id)
            call.respond(mapOf("deleted" to true))
        }
    }
}