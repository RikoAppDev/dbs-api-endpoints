package dev.rikoapp.endpoints

import dev.rikoapp.databases.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.sql.Connection

fun Application.setupV2Routing(dbConnection: Connection, dbSchema: DatabaseSchema) {
    routing {
        // Return all commenters
        get("/v2/posts/{postId}/users") {
            val postId = call.parameters["postId"]?.toIntOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
            try {
                val allCommenters = dbSchema.getCommenters(dbConnection, postId)
                call.respond(HttpStatusCode.OK, mapOf("items" to allCommenters))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }

        // Return all friends
        get("/v2/users/{userId}/friends") {
            val userId = call.parameters["userId"]?.toIntOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
            try {
                val allFriends = dbSchema.getFriends(dbConnection, userId)
                call.respond(HttpStatusCode.OK, mapOf("items" to allFriends))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }

        // Return tags daily percentage occurrence
        get("/v2/tags/{tag}/stats") {
            val tag = call.parameters["tag"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            try {
                val tagDayInfo = dbSchema.getTagDayInfo(dbConnection, tag)
                call.respond(HttpStatusCode.OK, mapOf("result" to tagDayInfo))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }

        // Return posts
        get(Regex("/v2/posts(/)?")) {
            val duration =
                call.request.queryParameters["duration"]
            val limit =
                call.request.queryParameters["limit"]

            val query = call.request.queryParameters["query"]

            when {
                duration != null && limit != null -> {
                    try {
                        val allPosts = dbSchema.getPostsByDurationAndLimit(
                            connection = dbConnection,
                            dur = duration.toIntOrNull()
                                ?: return@get call.respond(HttpStatusCode.BadRequest),
                            limit = limit.toIntOrNull()
                                ?: return@get call.respond(HttpStatusCode.BadRequest)
                        )
                        call.respond(HttpStatusCode.OK, mapOf("items" to allPosts))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError)
                    }
                }

                limit != null && query != null -> {
                    try {
                        val allPosts = dbSchema.getPostsByLimitAndQuery(
                            connection = dbConnection,
                            limit = limit.toIntOrNull() ?: return@get call.respond(
                                HttpStatusCode.BadRequest
                            ),
                            query = query
                        )
                        call.respond(HttpStatusCode.OK, mapOf("items" to allPosts))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError)
                    }
                }
            }
        }
    }
}