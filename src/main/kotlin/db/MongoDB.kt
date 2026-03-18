package com.example.db

import com.example.model.Atraccion
import com.example.model.Multimedia
import com.example.model.ReinicioConfig
import com.example.model.TextoGuardado
import com.example.model.Turno
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.coroutine.CoroutineDatabase

object MongoDB {

    //private val client = KMongo.createClient().coroutine
    private val client = KMongo.createClient("mongodb://localhost:27017").coroutine

    val database: CoroutineDatabase =
        client.getDatabase("pueblito_barro_db")

    val atracciones = database.getCollection<Atraccion>()
    val turnos = database.getCollection<Turno>("turno")
    val textos = database.getCollection<TextoGuardado>()
    val multimedia = database.getCollection<Multimedia>()
    val configuracionCollection = database.getCollection<ReinicioConfig>("configuracion")
}