package com.rakangsoftware.kmmdemo.domain

interface PostRepository {
    suspend fun getById(id: Int): Post

    suspend fun getAll(): List<Post>
}
