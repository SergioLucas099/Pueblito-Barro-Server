package com.example.websocket

import io.ktor.server.websocket.WebSocketServerSession
import io.ktor.websocket.Frame
import java.util.concurrent.CopyOnWriteArraySet

object TextosGuardadosManager {

    private val sessions = CopyOnWriteArraySet<WebSocketServerSession>()

    fun add(session: WebSocketServerSession) {
        AtraccionesSocketManager.sessions.add(session)
    }

    fun remove(session: WebSocketServerSession) {
        AtraccionesSocketManager.sessions.remove(session)
    }

    suspend fun broadcast(message: String) {
        AtraccionesSocketManager.sessions.forEach {
            it.send(Frame.Text(message))
        }
    }
}