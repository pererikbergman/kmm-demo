package com.rakangsoftware.kmmdemo.data.database

import com.rakangsoftware.kmmdemo.database.AppDatabase
import com.rakangsoftware.kmmdemo.database.PostDto
import com.rakangsoftware.kmmdemo.domain.Post
import com.rakangsoftware.kmmdemo.domain.PostRepository

class PostRepositorySQLDelight(databaseDriverFactory: DatabaseDriverFactory) : PostRepository {
    private val database = AppDatabase(databaseDriverFactory.createDriver())
    private val dbQuery = database.appDatabaseQueries

    internal fun clearDatabase() {
        dbQuery.transaction {
            dbQuery.removeAllPostDtos()
        }
    }

    override suspend fun getById(id: Int): Post =
        dbQuery.selectPostDtoById(id.toLong(), ::mapPostSelecting)
            .executeAsOne()

    override suspend fun getAll(): List<Post> {
        return dbQuery.selectAllPostDto()
            .executeAsList()
            .map { it.toPost() }
    }

    private fun mapPostSelecting(
        userId: Long,
        id: Long,
        title: String?,
        body: String?,
    ): Post {
        return Post(
            userId = userId.toInt(),
            id = id.toInt(),
            title = title ?: "",
            body = body ?: ""
        )
    }

    private fun PostDto.toPost(): Post = Post(
        userId = userId.toInt(),
        id = id.toInt(),
        title = title ?: "",
        body = body ?: ""
    )
}
