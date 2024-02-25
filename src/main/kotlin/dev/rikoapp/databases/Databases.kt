package dev.rikoapp.databases

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.sql.*
import kotlinx.coroutines.*

private const val SELECT_DB_VERSION = "SELECT version()"

fun Application.configureDatabases() {
    val dbConnection: Connection = connectToPostgres()

    install(ContentNegotiation) {
        json()
    }

    routing {
        // Return database version
        get("/v1/status") {
            try {
                val dbVersion = getDbVersion(dbConnection)
                call.respond(HttpStatusCode.OK, mapOf("version" to dbVersion))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }
}

suspend fun getDbVersion(connection: Connection): String = withContext(Dispatchers.IO) {
    val statement = connection.prepareStatement(SELECT_DB_VERSION)
    val result = statement.executeQuery()

    if (result.next()) {
        val dbVersion = result.getString("version")
        return@withContext dbVersion
    } else {
        throw Exception("Error while getting database version!")
    }
}

fun connectToPostgres(): Connection {
    Class.forName("org.postgresql.Driver")
    val host = System.getenv("DATABASE_HOST")
    val port = System.getenv("DATABASE_PORT")
    val dbName = System.getenv("DATABASE_NAME")
    val user = System.getenv("DATABASE_USER")
    val password = System.getenv("DATABASE_PASSWORD")
    val url = "jdbc:postgresql://$host:$port/$dbName"

    return DriverManager.getConnection(url, user, password)
}
