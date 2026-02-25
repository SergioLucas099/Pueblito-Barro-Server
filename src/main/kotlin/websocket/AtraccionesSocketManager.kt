package com.example.websocket

import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.util.concurrent.CopyOnWriteArraySet

object AtraccionesSocketManager {

    val sessions = CopyOnWriteArraySet<WebSocketServerSession>()

    fun add(session: WebSocketServerSession) {
        println("🔌 Cliente conectado a ATRACCIONES")
        sessions.add(session)
    }

    fun remove(session: WebSocketServerSession) {
        println("❌ Cliente desconectado ATRACCIONES")
        sessions.remove(session)
    }

    suspend fun broadcast(message: String) {
        println("📢 Enviando mensaje '$message' a ${sessions.size} sesiones")
        sessions.forEach {
            it.send(Frame.Text(message))
        }
    }
}