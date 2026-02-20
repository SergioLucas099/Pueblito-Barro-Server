package com.example.plugins

import com.example.websocket.AtraccionesSocketManager
import com.example.websocket.MultimediaSocketManager
import com.example.websocket.TextosGuardadosManager
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlin.time.Duration.Companion.seconds

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    routing {
        webSocket("/ws/atracciones") {

            AtraccionesSocketManager.add(this)

            try {
                for (frame in incoming) {
                    // No necesitamos recibir mensajes por ahora
                }
            } finally {
                AtraccionesSocketManager.remove(this)
            }
        }

        webSocket("/ws/multimedia") {

            MultimediaSocketManager.add(this)

            try {
                for (frame in incoming) {
                    // No necesitamos recibir mensajes
                }
            } finally {
                MultimediaSocketManager.remove(this)
            }
        }

        webSocket("/ws/textos_guardados"){

            TextosGuardadosManager.add(this)

            try {
                for (frame in incoming) {
                    // No necesitamos recibir mensajes
                }
            } finally {
                TextosGuardadosManager.remove(this)
            }
        }

    }
}