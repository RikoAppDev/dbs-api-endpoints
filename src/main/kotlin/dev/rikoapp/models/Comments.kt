package dev.rikoapp.models

import kotlinx.serialization.Serializable

@Serializable
data class Comments(
    val id: Int,
    val displayname: String?,
    val body: String?,
    val text: String?,
    val score: Int,
    val position: Int
)
