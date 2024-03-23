package dev.rikoapp.models

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import org.jetbrains.annotations.Nullable

@Serializable
data class PostsWithDuration(
    val id: Int,
    val creationdate: Instant,
    @Nullable
    val viewcount: Int?,
    @Nullable
    val lasteditdate: Instant?,
    @Nullable
    val lastactivitydate: Instant?,
    @Nullable
    val title: String?,
    @Nullable
    val closeddate: Instant?,
    val duration: Float
)

@Serializable
data class PostsWithTags(
    val id: Int,
    val creationdate: Instant,
    @Nullable
    val viewcount: Int?,
    @Nullable
    val lasteditdate: Instant?,
    @Nullable
    val lastactivitydate: Instant?,
    @Nullable
    val title: String?,
    @Nullable
    val body: String?,
    @Nullable
    val answercount: Int?,
    @Nullable
    val closeddate: Instant?,
    @Nullable
    val tags: Array<String>?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PostsWithTags

        if (id != other.id) return false
        if (viewcount != other.viewcount) return false
        if (lasteditdate != other.lasteditdate) return false
        if (lastactivitydate != other.lastactivitydate) return false
        if (title != other.title) return false
        if (body != other.body) return false
        if (answercount != other.answercount) return false
        if (closeddate != other.closeddate) return false
        if (tags != null) {
            if (other.tags == null) return false
            if (!tags.contentEquals(other.tags)) return false
        } else if (other.tags != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + (viewcount ?: 0)
        result = 31 * result + (lasteditdate?.hashCode() ?: 0)
        result = 31 * result + (lastactivitydate?.hashCode() ?: 0)
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (body?.hashCode() ?: 0)
        result = 31 * result + (answercount ?: 0)
        result = 31 * result + (closeddate?.hashCode() ?: 0)
        result = 31 * result + (tags?.contentHashCode() ?: 0)
        return result
    }
}

@Serializable
data class PostBadges(
    val id: Int,
    val title: String?,
    val type: String,
    val created_at: Instant,
    val position: Int
)

@Serializable
data class PostsWithCommentsInfo(
    val post_id: Int,
    val title: String?,
    val displayname: String?,
    val text: String?,
    val created_at: Instant,
    val diff: String,
    val avg: String
)

@Serializable
data class PostThread(
    val displayname: String?,
    val body: String?,
    val created_at: Instant
)