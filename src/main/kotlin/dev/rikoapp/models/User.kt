package dev.rikoapp.models

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import org.jetbrains.annotations.Nullable

@Serializable
data class User(
    val id: Int,
    val reputation: Int,
    val creationdate: Instant,
    val displayname: String,
    @Nullable
    val lastaccessdate: Instant?,
    @Nullable
    val websiteurl: String?,
    @Nullable
    val location: String?,
    @Nullable
    val aboutme: String?,
    val views: Int,
    val upvotes: Int,
    val downvotes: Int,
    @Nullable
    val profileimageurl: String?,
    @Nullable
    val age: Int?,
    @Nullable
    val accountid: Int?
)
