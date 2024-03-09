package dev.rikoapp

import dev.rikoapp.databases.DatabaseSchema
import dev.rikoapp.databases.DatabaseSingleton
import dev.rikoapp.endpoints.setupV1Routing
import dev.rikoapp.endpoints.setupV2Routing
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json
import java.sql.Connection

fun main() {
    embeddedServer(Netty, port = 8000, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
        })
    }

    val dbConnection: Connection = DatabaseSingleton.connectToPostgres()
    val dbSchema = DatabaseSchema()

    setupV1Routing(dbConnection, dbSchema)
    setupV2Routing(dbConnection, dbSchema)
}
