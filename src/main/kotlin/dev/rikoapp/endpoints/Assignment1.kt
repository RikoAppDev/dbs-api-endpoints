package dev.rikoapp.endpoints

import dev.rikoapp.databases.DatabaseSchema
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.sql.Connection


fun Application.setupV1Routing(dbConnection: Connection, dbSchema: DatabaseSchema) {
    routing {
        // Return database version
        get("/v1/status") {
            try {
                val dbVersion = dbSchema.getDbVersion(dbConnection)
                call.respond(HttpStatusCode.OK, mapOf("version" to dbVersion))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }
}