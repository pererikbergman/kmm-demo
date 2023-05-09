package com.rakangsoftware.kmmdemo.data.network

import com.rakangsoftware.kmmdemo.domain.Post
import com.rakangsoftware.kmmdemo.domain.PostRepository
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.*

class PostRepositoryKtor : PostRepository {
    private val client = HttpClient() {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }
    }

    override suspend fun getById(id: Int): Post =
        client.get("https://jsonplaceholder.typicode.com/posts/$id").body()

    override suspend fun getAll(): List<Post> =
        client.get("https://jsonplaceholder.typicode.com/posts/").body()
}
