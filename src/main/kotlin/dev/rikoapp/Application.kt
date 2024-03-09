package dev.rikoapp

import dev.rikoapp.databases.DatabaseSchema
import dev.rikoapp.databases.DatabaseSingleton
import dev.rikoapp.endpoints.setupSwagger
import dev.rikoapp.endpoints.setupV1Routing
import dev.rikoapp.endpoints.setupV2Routing
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
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

    routing {
        get {
            call.respondText(
                "<h1>Welcome to the RikoAppDev Project</h1>\n" +
                        "    <p>\n" +
                        "        This project contains the implementation of endpoints from the DBS assignment.\n" +
                        "        Visit the <a href=\"/openapi\">documentation</a> or check the project repository on\n" +
                        "        <a href=\"https://github.com/FIIT-Databases/dbs24-masarykova-str10-RikoAppDev\">GitHub</a>.\n" +
                        "    </p>",
                ContentType.Text.Html
            )
        }
    }

    val dbConnection: Connection = DatabaseSingleton.connectToPostgres()
    val dbSchema = DatabaseSchema()

    setupV1Routing(dbConnection, dbSchema)
    setupV2Routing(dbConnection, dbSchema)
    setupSwagger()
}
