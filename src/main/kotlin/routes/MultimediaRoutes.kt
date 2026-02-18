package com.example.routes

import com.example.db.MongoDB
import com.example.model.Multimedia
import io.ktor.http.*
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.litote.kmongo.setTo
import org.litote.kmongo.eq
import java.io.File
import java.util.*

fun Route.multimediaRoutes() {

    route("/multimedia") {

        // 📤 SUBIR VIDEO Y ACTIVARLO
        post("/upload") {

            val multipart = call.receiveMultipart()
            var savedFilePath: String? = null

            val uploadDir = File("videos")
            if (!uploadDir.exists()) {
                uploadDir.mkdirs()
            }

            multipart.forEachPart { part ->

                if (part is PartData.FileItem) {

                    val originalFileName =
                        part.originalFileName ?: "video.mp4"

                    if (!originalFileName.endsWith(".mp4")) {
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

                part.dispose()
            }

            if (savedFilePath == null) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "No se recibió archivo")
                )
            }

            // 🔥 1️⃣ Desactivar todos los anteriores
            MongoDB.multimedia.updateMany(
                org.litote.kmongo.EMPTY_BSON,
                Multimedia::activo setTo false
            )

            // 🔥 2️⃣ Insertar nuevo como activo
            val nuevoVideo = Multimedia(
                tipo = "VIDEO",
                url = savedFilePath!!,
                activo = true
            )

            MongoDB.multimedia.insertOne(nuevoVideo)

            call.respond(nuevoVideo)
        }

        // 📥 LISTAR TODOS
        get {
            val lista = MongoDB.multimedia.find().toList()
            call.respond(lista)
        }

        // 📺 OBTENER SOLO EL ACTIVO (IMPORTANTE PARA TurnosTV)
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

        // 🗑 ELIMINAR
        delete("/{id}") {
            val id = call.parameters["id"]!!
            MongoDB.multimedia.deleteOneById(id)
            call.respond(mapOf("deleted" to true))
        }
    }
}