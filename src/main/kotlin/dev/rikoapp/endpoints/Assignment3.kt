package dev.rikoapp.endpoints

import dev.rikoapp.databases.DatabaseSchema
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.sql.Connection

fun Application.setupV3Routing(dbConnection: Connection, dbSchema: DatabaseSchema) {
    routing {
        get("/v3/users/{userId}/badge_history") {
            val userId = call.parameters["userId"]?.toIntOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
            try {
                val postsBadges = dbSchema.getPostBadges(dbConnection, userId)
                call.respond(HttpStatusCode.OK, mapOf("items" to postsBadges))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }
        get("/v3/tags/{tag}/comments") {
            val tag = call.parameters["tag"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val count = call.request.queryParameters["count"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest)
            try {
                val posts = dbSchema.getPostsWithCommentsInfo(dbConnection, tag, count)
                call.respond(HttpStatusCode.OK, mapOf("items" to posts))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }
        get("/v3/tags/{tagName}/comments/{position}") {
            val tagName = call.parameters["tagName"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val position =
                call.parameters["position"]?.toIntOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
            val limit = call.request.queryParameters["limit"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest)
            try {
                val comments = dbSchema.getNthCommentsForPostsWithTag(dbConnection, tagName, position, limit)
                call.respond(HttpStatusCode.OK, mapOf("items" to comments))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }
        get("/v3/posts/{postId}") {
            val postId = call.parameters["postId"]?.toIntOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
            val limit = call.request.queryParameters["limit"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest)
            try {
                val postThread = dbSchema.getPostThread(dbConnection, postId, limit)
                call.respond(HttpStatusCode.OK, mapOf("items" to postThread))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }
}