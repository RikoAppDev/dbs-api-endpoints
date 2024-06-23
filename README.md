# DBS Assignment API

This project contains the implementation of endpoints for the Database Systems assignment.

- For swagger gui for endpoints description visit `/openapi` endpoint.

---

## Endpoints

- Assignment 1: [v1 Endpoint](#v1-endpoint)
- Assignment 2: [v2 Endpoints](#v2-endpoints)
- Assignment 3: [v3 Endpoints](#v3-endpoints)

---

## V1 Endpoint

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

## V2 Endpoints

### 1. Retrieve Users for a Specific Post

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
- SQL query: [`SELECT_ALL_COMMENTERS`](#1-select_all_commenters)

### 2. Retrieve Users (Friends) for a Specific User

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
- SQL query: [`SELECT_ALL_FRIENDS`](#2-select_all_friends)

### 3. Retrieve Tag Statistics

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
- SQL query: [`SELECT_TAG_DAYS_INFO`](#3-select_tag_days_info)

### 4. Retrieve Posts with Various Parameters

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
- SQL query for duration&limit: [`SELECT_TAG_DAYS_INFO`](#4-select_all_posts_with_duration)
- SQL query for limit&query: [`SELECT_TAG_DAYS_INFO`](#5-select_all_posts_with_tags)

## V3 Endpoints

### 1. Retrieve User Badges with Previous Posts

- **Endpoint:** `/v3/users/{userId}/badge_history`
- **Method:** `GET`
- **Description:** Retrieve a list of posts along with the badges acquired by the users who authored those previous
  posts.
- **Parameters:**
    - `userId` (path, required, integer): ID of the user
- **Response:**
    - Status: 200 OK
    - Content Type: `application/json`
    - Schema: [PostBadges](#postbadges)
    - Example for `/v3/users/1465/badge_history`:
      ```json
      {
        "items": [
          {
            "id": 2500,
            "title": "Can Cat6 UTP stranded cable fit regular Cat6 Jacks?",
            "type": "post",
            "created_at": "2009-07-15T13:40:20.667Z",
            "position": 1
          },
          {
            "id": 2269,
            "title": "Student",
            "type": "badge",
            "created_at": "2009-07-15T13:42:30.973Z",
            "position": 1
          },
          {
            "id": 2540,
            "title": null,
            "type": "post",
            "created_at": "2009-07-15T13:47:40.243Z",
            "position": 2
          },
          {
            "id": 2341,
            "title": "Editor",
            "type": "badge",
            "created_at": "2009-07-15T13:57:30.973Z",
            "position": 2
          }
        ]
      }    
      ```
- SQL query: [`SELECT_ANALYSED_USER`](#1-select_analysed_user)

### 2. Retrieve Post Comments with Average Response Time

- **Endpoint:** `/v3/tags/{tag}/comments?count={count}`
- **Method:** `GET`
- **Description:** Retrieve a list of comments with calculated average response time between individual comments within
  posts that have more than the specified count of comments. The output includes how the average response time changed
  with increasing comments.
- **Parameters:**
    - `tag` (path, required, string): Name of the tag
    - `count` (query, required, integer): More than number of comments within posts
- **Response:**
    - Status: 200 OK
    - Content Type: `application/json`
    - Schema: [PostsWithCommentsInfo](#postswithcommentsinfo)
    - Example for `/v3/tags/alarm-clock/comments?count=2`:
      ```json
      {
        "items": [
          {
            "post_id": 855132,
            "title": "Set Windows 8.1 Alarms app to ring forever till the user stop it",
            "displayname": "Ramhound",
            "text": "Use a different application.",
            "post_created_at": "2014-12-21T00:44:33.810Z",
            "created_at": "2014-12-21T01:10:25.533Z",
            "diff": "00:25:51.723",
            "avg": "00:25:51.723"
          },
          {
            "post_id": 855132,
            "title": "Set Windows 8.1 Alarms app to ring forever till the user stop it",
            "displayname": "Tyson",
            "text": "I'll bet there is a registry tweak for this windows 8 pre-installed app, but I couldn't locate it",
            "post_created_at": "2014-12-21T00:44:33.810Z",
            "created_at": "2014-12-21T02:35:21.087Z",
            "diff": "01:24:55.554",
            "avg": "00:55:23.6385"
          },
          {
            "post_id": 855132,
            "title": "Set Windows 8.1 Alarms app to ring forever till the user stop it",
            "displayname": "Omar",
            "text": "@Ramhound Could you please recommend any app similar to Windows' application? I love this good looking Windows app, and I won't install any other app if it really deserves taking my computer resources and have similar capabilities to the Windows' app.",
            "post_created_at": "2014-12-21T00:44:33.810Z",
            "created_at": "2014-12-21T03:07:26.967Z",
            "diff": "00:32:05.88",
            "avg": "00:47:37.719"
          },
          {
            "post_id": 1759203,
            "title": "How to stop Windows Alarms (Clock) app from asking access permissions on startup without saying yes or signing in?",
            "displayname": "John",
            "text": "Not here on any Windows 10 / 11 machine.  Run DISM / SFC and test .......   (1) Open cmd.exe with Run as Administrator.\n(2) DISM.exe /Online /Cleanup-image /StartComponentCleanup\n(3) DISM.exe /Online /Cleanup-Image /Restorehealth\n(4) SFC /SCANNOW\n(5) Restart when all the above is complete and test.",
            "post_created_at": "2022-12-23T10:45:04.227Z",
            "created_at": "2022-12-23T13:50:46.843Z",
            "diff": "03:05:42.616",
            "avg": "03:05:42.616"
          },
          {
            "post_id": 1759203,
            "title": "How to stop Windows Alarms (Clock) app from asking access permissions on startup without saying yes or signing in?",
            "displayname": "Nagev",
            "text": "Thanks, need some time to research and understand that before I try it.",
            "post_created_at": "2022-12-23T10:45:04.227Z",
            "created_at": "2022-12-23T17:59:11.300Z",
            "diff": "04:08:24.457",
            "avg": "03:37:03.5365"
          },
          {
            "post_id": 1759203,
            "title": "How to stop Windows Alarms (Clock) app from asking access permissions on startup without saying yes or signing in?",
            "displayname": "John",
            "text": "DISM and SFC is not at all risky to run .  SFC may take some time if much to repair",
            "post_created_at": "2022-12-23T10:45:04.227Z",
            "created_at": "2022-12-23T23:10:51.883Z",
            "diff": "05:11:40.583",
            "avg": "04:08:35.885333"
          }
        ]
      }
      ```

- SQL query: [`SELECT_ALL_SUITABLE_POSTS_FOR_TAG`](#2-select_all_suitable_posts_for_tag)

### 3. Retrieve n-th Comments for Post with Tag

- **Endpoint:** `/v3/tags/{tagname}/comments/{position}?limit={limit}`
- **Method:** `GET`
- **Description:** Retrieve a list comments for posts with the specified tag, which were created as the n-th `position`
  comments in order, sorted by creation date with a limit of `limit`.
- **Parameters:**
    - `tagname` (path, required, string): Tag name for filtering posts
    - `position` (path, required, integer): Position of the comment in order
    - `limit` (query, required, integer): Limit for the number of comments returned
- **Response:**
    - Status: 200 OK
    - Content Type: `application/json`
    - Schema: [Comments](#comments)
    - Example for `/v3/tags/linux/comments/2?limit=1`:
      ```json
      {
        "items": [
          {
            "id": 745427,
            "displayname": "Oliver Salzburg",
            "body": "<p>I am running Kubuntu Hardy Heron, with a dual monitor setup, and have VirtualBox on it running Windows XP in seamless mode.</p>\n\n<p>My problem is, I can't get VirtualBox to extend to the second monitor. \nHow can this be achieved?</p>\n",
            "text": "http://ubuntuforums.org/showthread.php?t=433359",
            "score": 0,
            "position": 2
          }
        ]
      }
      ```

- SQL query: [`SELECT_NTH_COMMENTS_FOR_TAG`](#3-select_nth_comments_for_tag)

### 4. Retrieve Thread for Post

- **Endpoint:** `/v3/posts/{postid}?limit={limit}`
- **Method:** `GET`
- **Description:** Retrieve a thread for the post with the specified ID `postid`. The thread starts with the
  post itself and continues with child posts where `postid` is the `parentid`, sorted by creation date from oldest to
  newest.
- **Parameters:**
    - `postid` (path, required, integer): ID of the post
    - `limit` (query, required, integer): Limit for the number of thread posts returned
- **Response:**
    - Status: 200 OK
    - Content Type: `application/json`
    - Schema: [PostThread](#postthread)
    - Example for `/v3/posts/2154?limit=2`:
      ```json
        {
          "items": [
            {
              "displayname": "Eugene M",
              "body": "<p>So, I'm a technology guy and sometimes I have to troubleshoot a home network, including my own. I make sure the wires are in securely and that the lights suggest there's an actual internet connection. Usually after that point I just reset the router( and possibly the cable modem) and that fixes things most of the time.</p>\n\n<p>The problem is I'd like to know what sort of issue I could possibly be fixing by resetting the router.</p>\n\n<p>EDIT: Just to clarify, I was speaking more about reset as in turning the router off and on. Still, any information about a hard reset(paperclip in the hole) is useful. So the more accurate term would probably be restarting </p>\n\n<p>Also, personally I usually have to deal with D-Link or Linksys home routers. I generally only bother messing around with stuff if I can't make a connection to the internet at all.</p>\n",
              "created_at": "2009-07-15T12:51:57.340Z"
            },
            {
              "displayname": "Ólafur Waage",
              "body": "<p>Every router has it's original firmware stored somewhere on it.</p>\n\n<p>When you reset the router you overwrite the current firmware and config with the original one. What usually is fixing the problem is that the config is overwritten with the original one. But in some cases you have an updated router that isn't working for some reason.</p>\n",
              "created_at": "2009-07-15T12:54:48.507Z"
            }
          ]
        }
      ```

- SQL query: [`SELECT_WHOLE_THREAD`](#4-select_whole_thread)

---

## Queries

- Assignment 1: [v1 Query](#v1-query)
- Assignment 2: [v2 Queries](#v2-queries)
- Assignment 3: [v3 Queries](#v3-queries)

---

## V1 Query

### 1. `SELECT_DB_VERSION`

- ***Description:*** Retrieves the version of the PostgreSQL database.
- ***Use Case:*** Checking the current database version.
     ```postgresql
     select version();
     ```

## V2 Queries

### 1. `SELECT_ALL_COMMENTERS`

- ***Description:*** This PostgreSQL query retrieves user details for individuals who have commented on a specific post,
  capturing the maximum creation date of their comments. It utilizes `JOIN` operations to connect the comments, posts,
  and users tables based on corresponding IDs. The `WHERE` clause filters the results to a specific post ID, and
  the `GROUP BY` clause ensures each user's information is provided only once. The final result is ordered in descending
  order based on the latest comment creation date.
- ***Use Case:*** Identifying users who engaged with a particular post.
   ```postgresql
   select u.*, max(c.creationdate) first_comment_date
   from comments c
          join posts p on p.id = c.postid
          join users u on c.userid = u.id
   where p.id = ?
   group by u.id, reputation, u.creationdate, displayname, lastaccessdate, websiteurl, location, aboutme, views, upvotes,
            downvotes, profileimageurl, age, accountid
   order by first_comment_date desc;
   ```

### 2. `SELECT_ALL_FRIENDS`

- ***Description:*** This PostgreSQL query retrieves user details for individuals who have interacted with a specified
  user
  by either commenting on their posts or posts they have commented on. It uses `UNION` to combine results from two
  separate conditions: first, where the user ID matches the commenter ID, and second, where the user ID matches the
  owner user ID of the post. The final result is ordered based on the creation date of the user account.
- ***Use Case:*** Finding friends based on mutual interactions.
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

### 3. `SELECT_TAG_DAYS_INFO`

- ***Description:*** This PostgreSQL query calculates the percentage representation of posts with a specific tag for
  each
  day of the week. It utilizes Common Table Expressions (CTEs): `tags_count` counts the number of posts with the given
  tag for each day, while `total_count` calculates the total number of posts for each day. The final query then computes
  the tag percentage by dividing the tag count by the total count, rounding to two decimal places, and ordering the
  results by day of the week.
- ***Use Case:*** Analyzing the distribution of posts with a specific tag throughout the week.
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

### 4. `SELECT_ALL_POSTS_WITH_DURATION`

- ***Description:*** This PostgreSQL query retrieves a list of the latest resolved posts, which were open for a maximum
  duration specified in minutes. The `WHERE` clause filters posts based on the condition that their duration of
  openness (calculated as the difference in minutes between closed and creation dates) is less than the specified
  threshold. The result is limited to the specified number of posts and sorted in descending order based on
  the `closeddate`. Each post in the list includes information such as id, creation date, view count, last edit date,
  last activity date, title, closed date, and the duration of unresolved in minutes.
- ***Use Case:*** Obtaining latest resolved posts that were open for a specific duration.
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

### 5. `SELECT_ALL_POSTS_WITH_TAGS`

- ***Description:***
  This PostgreSQL query uses a CTE named `posts_table` to filter posts based on case-insensitive
  matches in the title or body, using the `ILIKE` operator. The CTE limits and filters the result to a specified number
  of records and orders them by creation date in descending order. The main query then selects details from
  the `posts_table` and aggregates corresponding tag names using the `array_agg` function. This provides a comprehensive
  view of posts, including their ID, creation date, view count, title, body, answer count, and an array of associated
  tags. The results are grouped by post attributes and ordered by creation date in descending order, presenting a
  consolidated overview of posts with their respective tags.
- ***Use Case:*** Searching and retrieving posts with specific tags and filtering by a search query.
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

## V3 Queries

### 1. `SELECT_ANALYSED_USER`

- ***Description:***
  This PostgreSQL query selects posts and badges associated with a specific `userid` and analyzes the sequence
  of their creation dates. It utilizes window functions `lag()` and `lead()` to identify the transition points between
  posts and badges. The query then filters and orders the results based on whether a post is followed by a badge or vice
  versa, providing insight into the user's activity timeline and their badge obtainment.
- ***Use Case:*** Analyzing the activity history of a user by examining the sequence of posts and badges they have
  created or received.

  ```postgresql
  select post_id,
         title,
         type,
         created_at,
         row_number() over (partition by type order by created_at) position
  from (select *,
               lag(type) over (order by created_at)  before_type,
               lead(type) over (order by created_at) after_type
        from (select p.id           post_id,
                     p.title        title,
                     'post'         type,
                     p.creationdate created_at
              from posts p
              where p.owneruserid = ?
              union all
              select b.id    badge_id,
                     b.name  title,
                     'badge' type,
                     b.date  created_at
              from badges b
              where b.userid = ?) user_history) before_after_types
  where type = 'post' and after_type = 'badge'
     or type = 'badge' and before_type = 'post'
  order by created_at;
  ```

### 2. `SELECT_ALL_SUITABLE_POSTS_FOR_TAG`

- ***Description:***
  This PostgreSQL query retrieves posts associated with a specified `tagname` that have more than a certain number
  of comments `comment_count`. It calculates the time difference between consecutive comments `time_diff` for each
  post and computes the average time difference using `avg()` window function over the comment sequence. The query
  filters posts based on the specified tag, groups them by post attributes, and selects posts with a comment count
  exceeding the threshold.
- ***Use Case:*** Extracting posts with a particular tag that have generated significant discussion, allowing for
  further analysis of comment activity.

  ```postgresql
  select post_id,
         title,
         displayname,
         text,
         created_at,
         time_diff::text                                                      diff,
         avg(time_diff) over (partition by post_id order by created_at)::text avg
  from (select p.id                                                              post_id,
               p.title,
               u.displayname,
               c.text,
               c.creationdate                                                    created_at,
               c.creationdate - lag(c.creationdate, 1, p.creationdate)
                                over (partition by p.id order by c.creationdate) time_diff,
               count(c.id) over (partition by p.id)                              comment_count
        from posts p
                 join post_tags pt on p.id = pt.post_id
                 join tags t on t.id = pt.tag_id
                 join comments c on p.id = c.postid
                 left join users u on u.id = c.userid
        where t.tagname = ?
        group by p.id, p.title, u.displayname, c.text, c.creationdate, c.id) comments
  where comment_count > ?;
  ```

### 3. `SELECT_NTH_COMMENTS_FOR_TAG`

- ***Description:***
  This PostgreSQL query retrieves the comments associated with posts tagged with a specified `tagname` that are
  the n-th comment `position` in their respective post sequences. It selects comment attributes such as ID,
  author display name (`displayname`), comment body (`body`), comment text (`text`), score, and position within the
  post.
- ***Use Case:*** Fetching specific comments, such as the second comment, for posts with a particular tag for analysis
  or review.

  ```postgresql
  select id, displayname, body, text, score, position
  from (select c.id,
               u.displayname,
               p.body,
               c.text,
               c.score,
               row_number() over (partition by p.id) position
        from comments c
                 join posts p on p.id = c.postid
                 join post_tags pt on p.id = pt.post_id
                 join tags t on t.id = pt.tag_id
                 join users u on u.id = c.userid
        where t.tagname = ?
        group by p.id, p.creationdate, c.creationdate, c.id, u.displayname, p.body
        order by p.creationdate, c.creationdate) help
  where position = ?
  limit ?;
  ```

### 4. `SELECT_WHOLE_THREAD`

- ***Description:***
  This PostgreSQL query retrieves the entire thread associated with a specific post `post_id`. It selects
  user display names (`displayname`), post bodies (`body`), and creation dates (`created_at`) for both the specified
  post and its parent post, ordering the results by creation date and limiting the output to the first `limit`
  records.
- ***Use Case:*** Fetching the conversation thread for a particular post, including both the original post and
  its subsequent posts, for detailed analysis or review.

  ```postgresql
  select distinct u.displayname, p.body, p.creationdate created_at
  from comments c
           join posts p on p.id = c.postid
           join public.users u on u.id = p.owneruserid
  where p.id = ?
     or p.parentid = ?
  order by p.creationdate
  limit ?;
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

### PostBadges

- `id` (integer)
- `title` (string or null)
- `type` (string)
- `created_at` (string, date-time)
- `position` (integer)

### PostsWithCommentsInfo

- `post_id` (integer)
- `title` (string or null)
- `displayname` (string or null)
- `text` (string or null)
- `post_created_at` (string, date-time)
- `created_at` (string, date-time)
- `diff` (string)
- `avg` (string)

### Comments

- `id` (integer)
- `displayname` (string or null)
- `body` (string or null)
- `text` (string or null)
- `score` (integer)
- `position` (integer)

### PostThread

- `displayname` (string or null)
- `body` (string or null)
- `created_at` (string, date-time)

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

``` kotlin
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

Current version: 3.0.0

---

## Author

**Frederik Duvač**

- GitHub: [RikoAppDev](https://github.com/RikoAppDev)
- LinkedIn: [Frederik Duvač](https://www.linkedin.com/in/frederik-duva%C4%8D-237040241/)
- IG: [duvi_frederik03](https://www.instagram.com/duvi_frederik03/)