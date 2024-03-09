package dev.rikoapp.databases

import java.sql.Connection
import java.sql.DriverManager

object DatabaseSingleton {
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
}