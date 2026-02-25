package com.example.websocket

import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.util.concurrent.CopyOnWriteArraySet

object TurnosSocketManager {

    private val sessions = CopyOnWriteArraySet<DefaultWebSocketServerSession>()

    fun add(session: DefaultWebSocketServerSession) {
        sessions.add(session)
    }

    fun remove(session: DefaultWebSocketServerSession) {
        sessions.remove(session)
    }

    suspend fun broadcast(message: String) {
        sessions.forEach {
            it.send(Frame.Text(message))
        }
    }
}