package dev.rikoapp.endpoints

import io.ktor.server.application.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*

fun Application.setupSwagger() {
    routing {
        swaggerUI(path = "openapi")
    }
}