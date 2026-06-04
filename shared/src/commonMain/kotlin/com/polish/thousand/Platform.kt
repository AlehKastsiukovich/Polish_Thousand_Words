package com.polish.thousand

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform