package com.example.routes

import com.example.db.MongoDB
import com.example.model.TextoGuardado
import com.example.websocket.MultimediaSocketManager
import com.example.websocket.TextosGuardadosManager
import com.mongodb.client.model.Updates
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.litote.kmongo.EMPTY_BSON
import org.litote.kmongo.eq

fun Route.textosRoutes() {

    route("/textos") {

        // Crear Texto
        post {
            val texto = call.receive<TextoGuardado>()

            if (texto.texto.isBlank()) {
                return@post call.respond(
                    mapOf("error" to "Este campo no puede estar vacío")
                )
            }

            MongoDB.textos.insertOne(texto)

            // Broadcast en tiempo real
            TextosGuardadosManager.broadcast(
                Json.encodeToString(texto)
            )

            call.respond(texto)
        }

        // Listar todas
        get {
            val lista = MongoDB.textos.find().toList()
            call.respond(lista)
        }

        // OBTENER SOLO EL ACTIVO
        get("/activo") {
            val activo = MongoDB.textos
                .find(TextoGuardado::activo eq true)
                .toList()
                .firstOrNull()

            if (activo == null) {
                return@get call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("error" to "No hay texto activo")
                )
            }
        }

        // Actualizar
        put("/{id}") {
            val id = call.parameters["id"]!!
            val actualizado = call.receive<TextoGuardado>()
            MongoDB.textos.updateOneById(id, actualizado)
            call.respond(mapOf("success" to true))
        }

        // ⭐ ACTIVAR VIDEO SIN SUBIR UNO NUEVO
        put("/{id}/activar") {

            val id = call.parameters["id"]!!

            MongoDB.textos.updateMany(
                EMPTY_BSON,
                Updates.set("activo", false)
            )

            MongoDB.textos.updateOneById(
                id,
                Updates.set("activo", true)
            )

            TextosGuardadosManager.broadcast("UPDATE_VIDEO")

            call.respond(mapOf("success" to true))
        }

        // Eliminar
        delete("/{id}") {
            val id = call.parameters["id"]!!
            MongoDB.textos.deleteOneById(id)
            call.respond(mapOf("deleted" to true))
        }
    }
}