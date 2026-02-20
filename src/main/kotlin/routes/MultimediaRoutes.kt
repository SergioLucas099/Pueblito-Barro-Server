package com.example.routes

import com.example.db.MongoDB
import com.example.model.Multimedia
import com.example.websocket.MultimediaSocketManager
import com.mongodb.client.model.Updates
import io.ktor.http.*
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.litote.kmongo.EMPTY_BSON
import org.litote.kmongo.eq
import java.io.File
import java.util.*

fun Route.multimediaRoutes() {

    route("/multimedia") {

        // 📤 SUBIR VIDEO Y ACTIVARLO
        post("/upload") {

            val multipart = call.receiveMultipart()

            var savedFilePath: String? = null
            var nombre: String = "Sin nombre"
            var sonido: Boolean = false

            val uploadDir = File("videos")
            if (!uploadDir.exists()) {
                uploadDir.mkdirs()
            }

            multipart.forEachPart { part ->

                when (part) {

                    is PartData.FileItem -> {

                        val originalFileName =
                            part.originalFileName ?: "video.mp4"

                        if (!originalFileName.endsWith(".mp4")) {
                            part.dispose()
                            return@forEachPart call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to "Solo se permiten MP4")
                            )
                        }

                        val uniqueName =
                            UUID.randomUUID().toString() + ".mp4"

                        val file = File(uploadDir, uniqueName)

                        part.streamProvider().use { input ->
                            file.outputStream().buffered().use { output ->
                                input.copyTo(output)
                            }
                        }

                        savedFilePath = "videos/$uniqueName"
                    }

                    is PartData.FormItem -> {

                        when (part.name) {
                            "sonido" -> sonido = part.value.toBoolean()
                            "nombre" -> nombre = part.value
                        }
                    }

                    else -> {}
                }

                part.dispose()
            }

            if (savedFilePath == null) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "No se recibió archivo")
                )
            }

            // 🔥 Desactivar todos correctamente
            MongoDB.multimedia.updateMany(
                EMPTY_BSON,
                Updates.set("activo", false)
            )

            val nuevoVideo = Multimedia(
                tipo = "VIDEO",
                url = savedFilePath!!,
                activo = true,
                sonido = sonido,
                nombre = nombre
            )

            MongoDB.multimedia.insertOne(nuevoVideo)

            MultimediaSocketManager.broadcast("UPDATE_VIDEO")

            call.respond(nuevoVideo)
        }

        // 📥 LISTAR TODOS
        get {
            val lista = MongoDB.multimedia.find().toList()
            call.respond(lista)
        }

        // 📺 OBTENER SOLO EL ACTIVO
        get("/activo") {

            val activo = MongoDB.multimedia
                .find(Multimedia::activo eq true)
                .toList()
                .firstOrNull()

            if (activo == null) {
                return@get call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("error" to "No hay video activo")
                )
            }

            call.respond(activo)
        }

        // 🗑 ELIMINAR (BD + archivo físico)
        delete("/{id}") {

            val id = call.parameters["id"]!!

            val media = MongoDB.multimedia.findOneById(id)

            if (media != null) {

                val file = File(media.url)

                if (file.exists()) {
                    file.delete()
                }

                MongoDB.multimedia.deleteOneById(id)

                MultimediaSocketManager.broadcast("UPDATE_VIDEO")
            }

            call.respond(mapOf("deleted" to true))
        }

        // ⭐ ACTIVAR VIDEO SIN SUBIR UNO NUEVO
        put("/{id}/activar") {

            val id = call.parameters["id"]!!

            MongoDB.multimedia.updateMany(
                EMPTY_BSON,
                Updates.set("activo", false)
            )

            MongoDB.multimedia.updateOneById(
                id,
                Updates.set("activo", true)
            )

            MultimediaSocketManager.broadcast("UPDATE_VIDEO")

            call.respond(mapOf("success" to true))
        }
    }
}
