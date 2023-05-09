package com.rakangsoftware.kmmdemo

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform