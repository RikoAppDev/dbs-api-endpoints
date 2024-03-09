package dev.rikoapp.databases

import dev.rikoapp.models.TagPerDayOfWeek
import dev.rikoapp.models.PostsWithDuration
import dev.rikoapp.models.PostsWithTags
import dev.rikoapp.models.User
import java.sql.*
import kotlinx.coroutines.*
import kotlinx.datetime.Instant

class DatabaseSchema {
    companion object {
        private const val SELECT_DB_VERSION = "select version();"
        private const val SELECT_ALL_COMMENTERS = "" +
                "select u.*, max(c.creationdate) first_comment_date\n" +
                "from comments c\n" +
                "         join posts p on p.id = c.postid\n" +
                "         join users u on c.userid = u.id\n" +
                "where p.id = ?\n" +
                "group by u.id, reputation, u.creationdate, displayname, lastaccessdate, websiteurl, location, aboutme, views, upvotes,\n" +
                "         downvotes, profileimageurl, age, accountid\n" +
                "order by first_comment_date desc;"
        private const val SELECT_ALL_FRIENDS = "" +
                "select u.*\n" +
                "from comments c\n" +
                "         join posts p on p.id = c.postid\n" +
                "         join users u on u.id = c.userid\n" +
                "where c.userid = ?\n" +
                "union\n" +
                "select u.*\n" +
                "from comments c\n" +
                "         join posts p on p.id = c.postid\n" +
                "         join users u on u.id = c.userid\n" +
                "where p.owneruserid = ?\n" +
                "order by creationdate;"
        private const val SELECT_TAG_DAYS_INFO = "" +
                "with tags_count as (select extract(dow from creationdate) day_of_week,\n" +
                "                           count(p.id)                    tag_count\n" +
                "                    from posts p\n" +
                "                             join post_tags pt on p.id = pt.post_id\n" +
                "                             join tags t on t.id = pt.tag_id\n" +
                "                    where t.tagname like ?\n" +
                "                    group by day_of_week),\n" +
                "     total_count as (select extract(dow from creationdate) day_of_week,\n" +
                "                            count(*)                       total_count\n" +
                "                     from posts p\n" +
                "                     group by day_of_week)\n" +
                "select tac.day_of_week,\n" +
                "       round(coalesce(tac.tag_count * 100.0 / nullif(toc.total_count, 0), 0), 2) tag_percentage\n" +
                "from total_count toc\n" +
                "         join tags_count tac on toc.day_of_week = tac.day_of_week\n" +
                "order by day_of_week;"
        private const val SELECT_ALL_POSTS_WITH_DURATION = "" +
                "select p.id,\n" +
                "       p.creationdate,\n" +
                "       p.viewcount,\n" +
                "       p.lasteditdate,\n" +
                "       p.lastactivitydate,\n" +
                "       p.title,\n" +
                "       p.closeddate,\n" +
                "       round(extract(epoch from (closeddate - creationdate)) / 60, 2) duration\n" +
                "from posts p\n" +
                "where round(extract(epoch from (closeddate - creationdate)) / 60, 2) < ?\n" +
                "order by closeddate desc\n" +
                "limit ?;"
        private const val SELECT_ALL_POSTS_WITH_TAGS = "" +
                "with posts_table as (select p.id,\n" +
                "                            p.creationdate,\n" +
                "                            p.viewcount,\n" +
                "                            p.lasteditdate,\n" +
                "                            p.lastactivitydate,\n" +
                "                            p.title,\n" +
                "                            p.body,\n" +
                "                            p.answercount,\n" +
                "                            p.closeddate\n" +
                "                     from posts p\n" +
                "                     where p.title ilike ?\n" +
                "                        or p.body ilike ?\n" +
                "                     order by creationdate desc\n" +
                "                     limit ?)\n" +
                "select p.*, array_agg(t.tagname) tags\n" +
                "from posts_table p\n" +
                "         left join post_tags pt on pt.post_id = p.id\n" +
                "         left join tags t on t.id = pt.tag_id\n" +
                "group by p.id, creationdate, viewcount, lasteditdate, lastactivitydate, title, body, answercount, closeddate\n" +
                "order by p.creationdate desc;"
    }

    suspend fun getCommenters(connection: Connection, postId: Int): ArrayList<User> = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_ALL_COMMENTERS)
        statement.setInt(1, postId)
        val resultSet = statement.executeQuery()
        val allCommenters = arrayListOf<User>()

        while (resultSet.next()) {
            val id = resultSet.getInt("id")
            val reputation = resultSet.getInt("reputation")
            val creationdate = resultSet.getTimestamp("creationdate").toInstant().let {
                Instant.fromEpochMilliseconds(it.toEpochMilli())
            }
            val displayname = resultSet.getString("displayname")
            val lastaccessdate = resultSet.getTimestamp("lastaccessdate")?.toInstant()?.let {
                Instant.fromEpochMilliseconds(it.toEpochMilli())
            }
            val websiteurl = resultSet.getString("websiteurl")
            val location = resultSet.getString("location")
            val aboutme = resultSet.getString("aboutme")
            val views = resultSet.getInt("views")
            val upvotes = resultSet.getInt("upvotes")
            val downvotes = resultSet.getInt("downvotes")
            val profileimageurl = resultSet.getString("profileimageurl")
            val age = resultSet.getObject("age").let {
                it?.toString()?.toIntOrNull()
            }
            val accountid = resultSet.getObject("accountid").let {
                it?.toString()?.toIntOrNull()
            }

            allCommenters.add(
                User(
                    id,
                    reputation,
                    creationdate,
                    displayname,
                    lastaccessdate,
                    websiteurl,
                    location,
                    aboutme,
                    views,
                    upvotes,
                    downvotes,
                    profileimageurl,
                    age,
                    accountid
                )
            )
        }

        return@withContext allCommenters
    }

    suspend fun getFriends(connection: Connection, userId: Int): ArrayList<User> = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_ALL_FRIENDS)
        statement.setInt(1, userId)
        statement.setInt(2, userId)
        val resultSet = statement.executeQuery()
        val allFriends = arrayListOf<User>()

        while (resultSet.next()) {
            val id = resultSet.getInt("id")
            val reputation = resultSet.getInt("reputation")
            val creationdate = resultSet.getTimestamp("creationdate").toInstant().let {
                Instant.fromEpochMilliseconds(it.toEpochMilli())
            }
            val displayname = resultSet.getString("displayname")
            val lastaccessdate = resultSet.getTimestamp("lastaccessdate")?.toInstant()?.let {
                Instant.fromEpochMilliseconds(it.toEpochMilli())
            }
            val websiteurl = resultSet.getString("websiteurl")
            val location = resultSet.getString("location")
            val aboutme = resultSet.getString("aboutme")
            val views = resultSet.getInt("views")
            val upvotes = resultSet.getInt("upvotes")
            val downvotes = resultSet.getInt("downvotes")
            val profileimageurl = resultSet.getString("profileimageurl")
            val age = resultSet.getObject("age").let {
                it?.toString()?.toIntOrNull()
            }
            val accountid = resultSet.getObject("accountid").let {
                it?.toString()?.toIntOrNull()
            }

            allFriends.add(
                User(
                    id,
                    reputation,
                    creationdate,
                    displayname,
                    lastaccessdate,
                    websiteurl,
                    location,
                    aboutme,
                    views,
                    upvotes,
                    downvotes,
                    profileimageurl,
                    age,
                    accountid
                )
            )
        }

        return@withContext allFriends
    }

    suspend fun getTagDayInfo(connection: Connection, tag: String): TagPerDayOfWeek = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_TAG_DAYS_INFO)
        statement.setString(1, tag)
        val resultSet = statement.executeQuery()
        val daysOfWeek = mutableMapOf<Int, Float>()

        while (resultSet.next()) {
            daysOfWeek[resultSet.getInt("day_of_week")] = resultSet.getFloat("tag_percentage")
        }

        return@withContext TagPerDayOfWeek(
            daysOfWeek.getOrDefault(1, 0f),
            daysOfWeek.getOrDefault(2, 0f),
            daysOfWeek.getOrDefault(3, 0f),
            daysOfWeek.getOrDefault(4, 0f),
            daysOfWeek.getOrDefault(5, 0f),
            daysOfWeek.getOrDefault(6, 0f),
            daysOfWeek.getOrDefault(0, 0f)
        )
    }

    suspend fun getPostsByDurationAndLimit(
        connection: Connection,
        dur: Int,
        limit: Int
    ): ArrayList<PostsWithDuration> = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_ALL_POSTS_WITH_DURATION)
        statement.setInt(1, dur)
        statement.setInt(2, limit)
        val resultSet = statement.executeQuery()
        val allPosts = arrayListOf<PostsWithDuration>()

        while (resultSet.next()) {
            val id = resultSet.getInt("id")
            val creationDate = resultSet.getTimestamp("creationdate").toInstant().let {
                Instant.fromEpochMilliseconds(it.toEpochMilli())
            }
            val viewCount = resultSet.getObject("viewcount").let {
                it?.toString()?.toIntOrNull()
            }
            val lastEditDate = resultSet.getTimestamp("lasteditdate")?.toInstant()?.let {
                Instant.fromEpochMilliseconds(it.toEpochMilli())
            }
            val lastActivityDate = resultSet.getTimestamp("lastactivitydate")?.toInstant()?.let {
                Instant.fromEpochMilliseconds(it.toEpochMilli())
            }
            val title = resultSet.getString("title")
            val closedDate = resultSet.getTimestamp("closeddate")?.toInstant()?.let {
                Instant.fromEpochMilliseconds(it.toEpochMilli())
            }
            val duration = resultSet.getFloat("duration")

            allPosts.add(
                PostsWithDuration(
                    id,
                    creationDate,
                    viewCount,
                    lastEditDate,
                    lastActivityDate,
                    title,
                    closedDate,
                    duration
                )
            )
        }

        return@withContext allPosts
    }

    suspend fun getPostsByLimitAndQuery(connection: Connection, limit: Int, query: String): ArrayList<PostsWithTags> =
        withContext(Dispatchers.IO) {
            val statement = connection.prepareStatement(SELECT_ALL_POSTS_WITH_TAGS)
            statement.setString(1, "%${query}%")
            statement.setString(2, "%${query}%")
            statement.setInt(3, limit)
            val resultSet = statement.executeQuery()
            val allPosts = arrayListOf<PostsWithTags>()

            while (resultSet.next()) {
                val id = resultSet.getInt("id")
                val creationDate = resultSet.getTimestamp("creationdate").toInstant().let {
                    Instant.fromEpochMilliseconds(it.toEpochMilli())
                }
                val viewCount = resultSet.getObject("viewcount").let {
                    it?.toString()?.toIntOrNull()
                }
                val lastEditDate = resultSet.getTimestamp("lasteditdate")?.toInstant()?.let {
                    Instant.fromEpochMilliseconds(it.toEpochMilli())
                }
                val lastActivityDate = resultSet.getTimestamp("lastactivitydate")?.toInstant()?.let {
                    Instant.fromEpochMilliseconds(it.toEpochMilli())
                }
                val title = resultSet.getString("title")
                val body = resultSet.getString("body")
                val answerCount = resultSet.getObject("answercount").let {
                    it?.toString()?.toIntOrNull()
                }
                val closedDate = resultSet.getTimestamp("closeddate")?.toInstant()?.let {
                    Instant.fromEpochMilliseconds(it.toEpochMilli())
                }
                val tags =
                    (resultSet.getArray("tags")?.array as? Array<*>)?.filterIsInstance<String>()?.toTypedArray()

                allPosts.add(
                    PostsWithTags(
                        id,
                        creationDate,
                        viewCount,
                        lastEditDate,
                        lastActivityDate,
                        title,
                        body,
                        answerCount,
                        closedDate,
                        tags
                    )
                )
            }

            return@withContext allPosts
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
}
