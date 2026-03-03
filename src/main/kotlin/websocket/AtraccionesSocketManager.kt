package com.example.websocket

import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.util.concurrent.CopyOnWriteArraySet

object AtraccionesSocketManager {

    val sessions = CopyOnWriteArraySet<WebSocketServerSession>()

    fun add(session: WebSocketServerSession) {
        sessions.add(session)
    }

    fun remove(session: WebSocketServerSession) {
        sessions.remove(session)
    }

    suspend fun broadcast(message: String) {
        sessions.forEach {
            it.send(Frame.Text(message))
        }
    }
}