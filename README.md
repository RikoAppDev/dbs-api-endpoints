# DBS Assignment API

This project contains the implementation of endpoints for the Database Systems assignment.

- For swagger gui for endpoints description visit `/openapi` endpoint.

---

## Endpoints

### 1. Retrieve Database Server Version

- **Endpoint:** `/v1/status`
- **Method:** `GET`
- **Description:** Retrieve the version information of the database server.
- **Response:**
    - Status: 200 OK
    - Content Type: `application/json`
    - Example:
      ```json
      {
        "version": "PostgreSQL 16.2, compiled by Visual C++ build 1937, 64-bit"
      }
      ```
- SQL query: [`SELECT_DB_VERSION`](#1-select_db_version)

### 2. Retrieve Users for a Specific Post

- **Endpoint:** `/v2/posts/{postId}/users`
- **Method:** `GET`
- **Description:** Retrieve a list of users for a specific post, ordered by the creation date of their comments.
- **Parameters:**
    - `postId` (path, required, integer): ID of the post
- **Response:**
    - Status: 200 OK
    - Content Type: `application/json`
    - Schema: [User](#user)
    - Example for `/v2/posts/1819157/users`:
      ```json
      {
        "items": [
          {
            "id": 1866388,
            "reputation": 1,
            "creationdate": "2023-12-01T00:05:24.3+00:00",
            "displayname": "TomR.",
            "lastaccessdate": "2023-12-03T06:18:19.607+00:00",
            "websiteurl": null,
            "location": null,
            "aboutme": null,
            "views": 1,
            "upvotes": 0,
            "downvotes": 0,
            "profileimageurl": null,
            "age": null,
            "accountid": 30035903
          }
        ]
      }
      ```
- SQL query: [`SELECT_ALL_COMMENTERS`](#2-select_all_commenters)

### 3. Retrieve Users (Friends) for a Specific User

- **Endpoint:** `/v2/users/{userId}/friends`
- **Method:** `GET`
- **Description:** Retrieve a list of users who have interacted with the specified user by commenting on his posts or
  posts he has commented on. Ordered by registration date, starting with the earliest registered users.
- **Parameters:**
    - `userId` (path, required, integer): ID of the user
- **Response:**
    - Status: 200 OK
    - Content Type: `application/json`
    - Schema: [User](#user)
    - Example for `/v2/users/1076348/friends`:
      ```json
      {
        "items": [
          {
            "id": 482362,
            "reputation": 10581,
            "creationdate": "2015-08-11T17:42:36.267+02",
            "displayname": "DrZoo",
            "lastaccessdate": "2023-12-03T06:41:11.75+01",
            "websiteurl": null,
            "location": null,
            "aboutme": null,
            "views": 1442,
            "upvotes": 555,
            "downvotes": 46,
            "profileimageurl": null,
            "age": null,
            "accountid": 2968677
          },
          {
            "id": 1076348,
            "reputation": 1,
            "creationdate": "2019-08-15T16:00:28.473+02",
            "displayname": "Richard",
            "lastaccessdate": "2019-09-10T16:57:48.527+02",
            "websiteurl": null,
            "location": null,
            "aboutme": null,
            "views": 0,
            "upvotes": 0,
            "downvotes": 0,
            "profileimageurl": null,
            "age": null,
            "accountid": 16514661
          }
        ]
      }
      ```
- SQL query: [`SELECT_ALL_FRIENDS`](#3-select_all_friends)

### 4. Retrieve Tag Statistics

- **Endpoint:** `/v2/tags/{tagname}/stats`
- **Method:** `GET`
- **Description:** Retrieve the percentage representation of posts with a specific tag for each day of the week, rounded
  to two decimal places.
- **Parameters:**
    - `tagname` (path, required, string): Name of the tag
- **Response:**
    - Status: 200 OK
    - Content Type: `application/json`
    - Schema: [TagPerDayOfWeek](#tagperdayofweek)
    - Example for `/v2/tags/linux/stats`:
      ```json
      {
        "result": {
          "monday": 4.71,
          "tuesday": 4.69,
          "wednesday": 4.63,
          "thursday": 4.57,
          "friday": 4.67,
          "saturday": 4.98,
          "sunday": 4.88
        }
      }
      ```
- SQL query: [`SELECT_TAG_DAYS_INFO`](#4-select_tag_days_info)

### 5. Retrieve Posts with Various Parameters

- **Endpoint:** `/v2/posts`
- **Method:** `GET`
- **Description:** Retrieve a list of posts with various parameters. Supports two separates pairs of
  parameters: `duration&limit`
  and `limit&query`.
- **Parameters:**
    - `duration` (query, required, integer): Duration in minutes for which posts were open
    - `limit` (query, required, integer): Limit the number of items in the response
    - `query` (query, required, string): Search string for posts.title and posts.body
- **Response:**
    - Status: 200 OK
    - Content Type: `application/json`
    - Schema: [PostWithDuration](#postwithduration) or [PostWithTags](#postwithtags)
    - Example for `/v2/posts?duration=5&limit=2`:
      ```json
      {
        "items": [
          {
            "id": 630686,
            "creationdate": "2013-08-11T16:47:19.513Z",
            "viewcount": 74,
            "lasteditdate": "2014-06-03T18:15:32.750Z",
            "lastactivitydate": "2014-06-03T18:15:32.750Z",
            "title": "How can I make a backup of my factory-new computer?",
            "closeddate": "2013-08-11T16:52:18.460Z",
            "duration": 4.98
          },
          {
            "id": 630766,
            "creationdate": "2013-08-11T23:08:07.227Z",
            "viewcount": 35,
            "lasteditdate": null,
            "lastactivitydate": "2013-08-11T23:08:07.227Z",
            "title": "What are our options for a Laptop/Notebook and Multi-Monitory support?",
            "closeddate": "2013-08-11T23:12:56.877Z",
            "duration": 4.83
          }
        ]
      }
      ```
    - Example for `/v2/posts?limit=1&query=linux`:
      ```json
      {
        "items": [
          {
            "id": 1819160,
            "creationdate": "2023-12-03T04:22:43.587Z",
            "viewcount": 7,
            "lasteditdate": null,
            "lastactivitydate": "2023-12-03T04:22:43.587Z",
            "title": "Keyboard not working on khali linux",
            "body": "<p>I have recently installed virtualbox on my windows 10 and trying to run Linux Ubuntu and Kali. Everything working on Ubuntu without any issue but when I am running kali it is not taking keyboard(Samsung bluetooth 500) input. Please can anyone help me out here.\nMany thanks in advance!!</p>\n",
            "answercount": 0,
            "closeddate": null,
            "tags": [
              "virtual-machine"
            ]
          }
        ]
      }
      ```
- SQL query for duration&limit: [`SELECT_TAG_DAYS_INFO`](#5-select_all_posts_with_duration)
- SQL query for limit&query: [`SELECT_TAG_DAYS_INFO`](#6-select_all_posts_with_tags)

---

## Queries Descriptions

### 1. `SELECT_DB_VERSION`

- _Description:_ Retrieves the version of the PostgreSQL database.
- _Use Case:_ Checking the current database version.
     ```postgresql
     select version();
     ```

### 2. `SELECT_ALL_COMMENTERS`

- _Description:_ This PostgreSQL query retrieves user details for individuals who have commented on a specific post,
  capturing the maximum creation date of their comments. It utilizes `JOIN` operations to connect the comments, posts,
  and users tables based on corresponding IDs. The `WHERE` clause filters the results to a specific post ID, and
  the `GROUP BY` clause ensures each user's information is provided only once. The final result is ordered in descending
  order based on the latest comment creation date.
- _Use Case:_ Identifying users who engaged with a particular post.
   ```postgresql
   select u.*, max(c.creationdate) first_comment_date
   from comments c
          join posts p on p.id = c.postid
          join users u on c.userid = u.id
   where p.id = 718
   group by u.id, reputation, u.creationdate, displayname, lastaccessdate, websiteurl, location, aboutme, views, upvotes,
            downvotes, profileimageurl, age, accountid
   order by first_comment_date desc;
   ```

### 3. `SELECT_ALL_FRIENDS`

- _Description:_ This PostgreSQL query retrieves user details for individuals who have interacted with a specified user
  by either commenting on their posts or posts they have commented on. It uses `UNION` to combine results from two
  separate conditions: first, where the user ID matches the commenter ID, and second, where the user ID matches the
  owner user ID of the post. The final result is ordered based on the creation date of the user account.
- _Use Case:_ Finding friends based on mutual interactions.
   ```postgresql
   select u.*
   from comments c
            join posts p on p.id = c.postid
            join users u on u.id = c.userid
   where c.userid = ?
   union
   select u.*
   from comments c
            join posts p on p.id = c.postid
            join users u on u.id = c.userid
   where p.owneruserid = ?
   order by creationdate;
   ```

### 4. `SELECT_TAG_DAYS_INFO`

- _Description:_ This PostgreSQL query calculates the percentage representation of posts with a specific tag for each
  day of the week. It utilizes Common Table Expressions (CTEs): `tags_count` counts the number of posts with the given
  tag for each day, while `total_count` calculates the total number of posts for each day. The final query then computes
  the tag percentage by dividing the tag count by the total count, rounding to two decimal places, and ordering the
  results by day of the week.
- _Use Case:_ Analyzing the distribution of posts with a specific tag throughout the week.
   ```postgresql
   with tags_count as (select extract(dow from creationdate) day_of_week,
                              count(p.id)                    tag_count
                       from posts p
                                join post_tags pt on p.id = pt.post_id
                                join tags t on t.id = pt.tag_id
                       where t.tagname like ?
                       group by day_of_week),
        total_count as (select extract(dow from creationdate) day_of_week,
                               count(*)                       total_count
                        from posts p
                        group by day_of_week)
   select tac.day_of_week,
          round(coalesce(tac.tag_count * 100.0 / nullif(toc.total_count, 0), 0), 2) tag_percentage
   from total_count toc
            join tags_count tac on toc.day_of_week = tac.day_of_week
   order by day_of_week;
   ```

### 5. `SELECT_ALL_POSTS_WITH_DURATION`

- _Description:_ This PostgreSQL query retrieves a list of the latest resolved posts, which were open for a maximum
  duration specified in minutes. The `WHERE` clause filters posts based on the condition that their duration of
  openness (calculated as the difference in minutes between closed and creation dates) is less than the specified
  threshold. The result is limited to the specified number of posts and sorted in descending order based on
  the `closeddate`. Each post in the list includes information such as id, creation date, view count, last edit date,
  last activity date, title, closed date, and the duration of unresolved in minutes.
- _Use Case:_ Obtaining latest resolved posts that were open for a specific duration.
   ```postgresql
   select p.id,
       p.creationdate,
       p.viewcount,
       p.lasteditdate,
       p.lastactivitydate,
       p.title,
       p.closeddate,
       round(extract(epoch from (closeddate - creationdate)) / 60, 2) duration
   from posts p
   where round(extract(epoch from (closeddate - creationdate)) / 60, 2) < ?
   order by closeddate desc
   limit ?;
   ```

### 6. `SELECT_ALL_POSTS_WITH_TAGS`

- _Description:_
  This PostgreSQL query uses a CTE named `posts_table` to filter posts based on case-insensitive
  matches in the title or body, using the `ILIKE` operator. The CTE limits and filters the result to a specified number
  of records and orders them by creation date in descending order. The main query then selects details from
  the `posts_table` and aggregates corresponding tag names using the `array_agg` function. This provides a comprehensive
  view of posts, including their ID, creation date, view count, title, body, answer count, and an array of associated
  tags. The results are grouped by post attributes and ordered by creation date in descending order, presenting a
  consolidated overview of posts with their respective tags.
- _Use Case:_ Searching and retrieving posts with specific tags and filtering by a search query.
   ```postgresql
   with posts_table as (select p.id,
                               p.creationdate,
                               p.viewcount,
                               p.lasteditdate,
                               p.lastactivitydate,
                               p.title,
                               p.body,
                               p.answercount,
                               p.closeddate
                        from posts p
                        where p.title ilike ?
                           or p.body ilike ?
                        order by creationdate desc
                        limit ?)
   select p.*, array_agg(t.tagname) tags
   from posts_table p
            left join post_tags pt on pt.post_id = p.id
            left join tags t on t.id = pt.tag_id
   group by p.id, creationdate, viewcount, lasteditdate, lastactivitydate, title, body, answercount, closeddate
   order by p.creationdate desc;
   ```

---

## Schema Definitions

### User

- `id` (integer)
- `reputation` (integer)
- `creationdate` (string, date-time)
- `displayname` (string)
- `lastaccessdate` (string, date-time)
- `websiteurl` (string or null)
- `location` (string or null)
- `aboutme` (string or null)
- `views` (integer)
- `upvotes` (integer)
- `downvotes` (integer)
- `profileimageurl` (string or null)
- `age` (integer or null)
- `accountid` (integer or null)

### TagPerDayOfWeek

- `monday` (float)
- `tuesday` (float)
- `wednesday` (float)
- `thursday` (float)
- `friday` (float)
- `saturday` (float)
- `sunday` (float)

### PostWithDuration

- `id` (integer)
- `creationdate` (string, date-time)
- `viewcount` (integer)
- `lasteditdate` (string or null, date-time)
- `lastactivitydate` (string, date-time)
- `title` (string)
- `closeddate` (string, date-time)
- `duration` (number, double)

### PostWithTags

- `id` (integer)
- `creationdate` (string)
- `viewcount` (integer)
- `lasteditdate` (string or null)
- `lastactivitydate` (string)
- `title` (string)
- `body` (string)
- `answercount` (integer)
- `closeddate` (string or null)
- `tags` (array of strings)

---

## Tech Stack

This project is built using the following technologies:

- **Kotlin:** A modern programming language that is expressive, concise, and fully interoperable with Java.

- **Ktor:** A framework for building asynchronous servers and clients in connected systems using Kotlin.

- **PostgreSQL:** An open-source relational database management system known for its reliability and robust features.

- **JSON:** A lightweight data interchange format commonly used for communication between server and client.

### Gradle build setup

For successful project build, clone the project into IntelliJ IDEA and make sure you have same libraries and versions
setup in `build.gradle.kts` and `gradle.properties` files.

#### Plugins

```kotlin
plugins {
    kotlin("jvm") version "1.9.22"
    id("io.ktor.plugin") version "2.3.8"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.23"
}
```

#### Properties

```
ktor_version=2.3.8
kotlin_version=1.9.22
logback_version=1.4.14
kotlin.code.style=official
postgres_version=42.5.1
h2_version=2.1.214
datetime=0.6.0-RC.2
```

#### Dependencies

```kotlin
dependencies {
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-swagger-jvm")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
    implementation("org.postgresql:postgresql:$postgres_version")
    implementation("com.h2database:h2:$h2_version")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:$datetime")
}
```

## Version

Current version: 2.0.0

---

## Author

**Frederik Duvač**

- GitHub: [RikoAppDev](https://github.com/RikoAppDev)
- LinkedIn: [Frederik Duvač](https://www.linkedin.com/in/frederik-duva%C4%8D-237040241/)
- IG: [duvi_frederik03](https://www.instagram.com/duvi_frederik03/)