package com.example.routes

import com.example.db.MongoDB
import com.example.model.TextoGuardado
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.textosRoutes() {

    route("/textos") {

        post {
            val texto = call.receive<TextoGuardado>()
            MongoDB.textos.insertOne(texto)
            call.respond(texto)
        }

        get {
            val lista = MongoDB.textos.find().toList()
            call.respond(lista)
        }

        put("/{id}") {
            val id = call.parameters["id"]!!
            val actualizado = call.receive<TextoGuardado>()
            MongoDB.textos.updateOneById(id, actualizado)
            call.respond(mapOf("success" to true))
        }

        delete("/{id}") {
            val id = call.parameters["id"]!!
            MongoDB.textos.deleteOneById(id)
            call.respond(mapOf("deleted" to true))
        }
    }
}