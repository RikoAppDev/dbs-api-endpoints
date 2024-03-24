package dev.rikoapp.databases

import dev.rikoapp.models.*
import java.sql.*
import kotlinx.coroutines.*
import kotlinx.datetime.Instant

class DatabaseSchema {
    companion object {
        // v1 - 1. endpoint
        private const val SELECT_DB_VERSION = "select version();"

        // v2 - 1. endpoint
        private const val SELECT_ALL_COMMENTERS = "" +
                "select u.*, max(c.creationdate) first_comment_date\n" +
                "from comments c\n" +
                "         join posts p on p.id = c.postid\n" +
                "         join users u on c.userid = u.id\n" +
                "where p.id = ?\n" +
                "group by u.id, reputation, u.creationdate, displayname, lastaccessdate, websiteurl, location, aboutme, views, upvotes,\n" +
                "         downvotes, profileimageurl, age, accountid\n" +
                "order by first_comment_date desc;"

        // v2 - 2. endpoint
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

        // v2 - 3. endpoint
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

        // v2 - 4. endpoint
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

        // v2 - 5. endpoint
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

        // v3 - 1. endpoint
        private const val SELECT_ANALYSED_USER = "" +
                "select post_id,\n" +
                "       title,\n" +
                "       type,\n" +
                "       created_at,\n" +
                "       row_number() over (partition by type order by created_at) position\n" +
                "from (select *,\n" +
                "             lag(type) over (order by created_at)  before_type,\n" +
                "             lead(type) over (order by created_at) after_type\n" +
                "      from (select p.id           post_id,\n" +
                "                   p.title        title,\n" +
                "                   'post'         type,\n" +
                "                   p.creationdate created_at\n" +
                "            from posts p\n" +
                "            where p.owneruserid = ?\n" +
                "            union all\n" +
                "            select b.id    badge_id,\n" +
                "                   b.name  title,\n" +
                "                   'badge' type,\n" +
                "                   b.date  created_at\n" +
                "            from badges b\n" +
                "            where b.userid = ?) user_history) before_after_types\n" +
                "where type = 'post' and after_type = 'badge'\n" +
                "   or type = 'badge' and before_type = 'post'\n" +
                "order by created_at;"

        // v3 - 2. endpoint
        private const val SELECT_ALL_SUITABLE_POSTS_FOR_TAG = "" +
                "select post_id,\n" +
                "       title,\n" +
                "       displayname,\n" +
                "       text,\n" +
                "       post_created_at,\n" +
                "       created_at,\n" +
                "       time_diff::text                                                      diff,\n" +
                "       avg(time_diff) over (partition by post_id order by created_at)::text avg\n" +
                "from (select p.id                                                              post_id,\n" +
                "             p.title,\n" +
                "             u.displayname,\n" +
                "             c.text,\n" +
                "             p.creationdate                                                    post_created_at,\n" +
                "             c.creationdate                                                    created_at,\n" +
                "             c.creationdate - lag(c.creationdate, 1, p.creationdate)\n" +
                "                              over (partition by p.id order by c.creationdate) time_diff,\n" +
                "             count(c.id) over (partition by p.id)                              comment_count\n" +
                "      from posts p\n" +
                "               join post_tags pt on p.id = pt.post_id\n" +
                "               join tags t on t.id = pt.tag_id\n" +
                "               join comments c on p.id = c.postid\n" +
                "               left join users u on u.id = c.userid\n" +
                "      where t.tagname = ?\n" +
                "      group by p.id, p.title, u.displayname, c.text, c.creationdate, c.id) comments\n" +
                "where comment_count > ?;"

        // v3 - 3. endpoint
        private const val SELECT_NTH_COMMENTS_FOR_TAG = "" +
                "select id, displayname, body, text, score, position\n" +
                "from (select c.id,\n" +
                "             u.displayname,\n" +
                "             p.body,\n" +
                "             c.text,\n" +
                "             c.score,\n" +
                "             row_number() over (partition by p.id) position\n" +
                "      from comments c\n" +
                "               join posts p on p.id = c.postid\n" +
                "               join post_tags pt on p.id = pt.post_id\n" +
                "               join tags t on t.id = pt.tag_id\n" +
                "               join users u on u.id = c.userid\n" +
                "      where t.tagname = ?\n" +
                "      group by p.id, p.creationdate, c.creationdate, c.id, u.displayname, p.body\n" +
                "      order by p.creationdate, c.creationdate) help\n" +
                "where position = ?\n" +
                "limit ?;"

        // v3 - 4. endpoint
        private const val SELECT_WHOLE_THREAD = "" +
                "select distinct u.displayname, p.body, p.creationdate created_at\n" +
                "from comments c\n" +
                "         join posts p on p.id = c.postid\n" +
                "         join public.users u on u.id = p.owneruserid\n" +
                "where p.id = ?\n" +
                "   or p.parentid = ?\n" +
                "order by p.creationdate\n" +
                "limit ?;"
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

    suspend fun getPostBadges(connection: Connection, userId: Int): ArrayList<PostBadges> =
        withContext(Dispatchers.IO) {
            val statement = connection.prepareStatement(SELECT_ANALYSED_USER)
            statement.setInt(1, userId)
            statement.setInt(2, userId)
            val resultSet = statement.executeQuery()
            val postsBadges = arrayListOf<PostBadges>()

            while (resultSet.next()) {
                val postId = resultSet.getInt("post_id")
                val title = resultSet.getString("title")
                val type = resultSet.getString("type")
                val createdAt = resultSet.getTimestamp("created_at").toInstant().let {
                    Instant.fromEpochMilliseconds(it.toEpochMilli())
                }
                val position = resultSet.getInt("position")

                postsBadges.add(
                    PostBadges(
                        postId, title, type, createdAt, position
                    )
                )
            }

            return@withContext postsBadges
        }

    suspend fun getPostsWithCommentsInfo(
        connection: Connection, tag: String, count: Int
    ): ArrayList<PostsWithCommentsInfo> = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_ALL_SUITABLE_POSTS_FOR_TAG)
        statement.setString(1, tag)
        statement.setInt(2, count)
        val resultSet = statement.executeQuery()
        val posts = arrayListOf<PostsWithCommentsInfo>()

        while (resultSet.next()) {
            val postId = resultSet.getInt("post_id")
            val title = resultSet.getString("title")
            val displayName = resultSet.getString("displayname")
            val text = resultSet.getString("text")
            val postCreatedAt = resultSet.getTimestamp("post_created_at").toInstant().let {
                Instant.fromEpochMilliseconds(it.toEpochMilli())
            }
            val createdAt = resultSet.getTimestamp("created_at").toInstant().let {
                Instant.fromEpochMilliseconds(it.toEpochMilli())
            }
            val diff = resultSet.getString("diff")
            val avg = resultSet.getString("avg")

            posts.add(PostsWithCommentsInfo(postId, title, displayName, text, postCreatedAt, createdAt, diff, avg))
        }

        return@withContext posts
    }

    suspend fun getNthCommentsForPostsWithTag(
        connection: Connection, tagName: String, pos: Int, limit: Int
    ): ArrayList<Comments> = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_NTH_COMMENTS_FOR_TAG)
        statement.setString(1, tagName)
        statement.setInt(2, pos)
        statement.setInt(3, limit)
        val resultSet = statement.executeQuery()
        val comments = arrayListOf<Comments>()

        while (resultSet.next()) {
            val id = resultSet.getInt("id")
            val displayName = resultSet.getString("displayname")
            val body = resultSet.getString("body")
            val text = resultSet.getString("text")
            val score = resultSet.getInt("score")
            val position = resultSet.getInt("position")

            comments.add(Comments(id, displayName, body, text, score, position))
        }

        return@withContext comments
    }


    suspend fun getPostThread(connection: Connection, postId: Int, limit: Int): ArrayList<PostThread> =
        withContext(Dispatchers.IO) {
            val statement = connection.prepareStatement(SELECT_WHOLE_THREAD)
            statement.setInt(1, postId)
            statement.setInt(2, postId)
            statement.setInt(3, limit)
            val resultSet = statement.executeQuery()
            val postThread = arrayListOf<PostThread>()

            while (resultSet.next()) {
                val displayName = resultSet.getString("displayname")
                val body = resultSet.getString("body")
                val createdAt = resultSet.getTimestamp("created_at").toInstant().let {
                    Instant.fromEpochMilliseconds(it.toEpochMilli())
                }

                postThread.add(PostThread(displayName, body, createdAt))
            }

            return@withContext postThread
        }
}
