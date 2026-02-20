package com.example

import com.example.plugins.configureRouting
import com.example.plugins.configureSerialization
import com.example.plugins.configureSockets
import io.ktor.server.application.*
import io.ktor.server.plugins.partialcontent.*
import io.ktor.server.plugins.defaultheaders.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {

    install(DefaultHeaders)
    install(PartialContent)

    configureSockets()
    configureSerialization()
    configureRouting()
}