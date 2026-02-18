package com.example.plugins

import io.ktor.server.application.*
import io.ktor.server.routing.*
import com.example.routes.*
import io.ktor.server.http.content.files
import io.ktor.server.http.content.static

fun Application.configureRouting() {
    routing {

        static("/videos") {
            files("videos")
        }

        atraccionesRoutes()
        turnosRoutes()
        multimediaRoutes()
        textosRoutes()
    }
}