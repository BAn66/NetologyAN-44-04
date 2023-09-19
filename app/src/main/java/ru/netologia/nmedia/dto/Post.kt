package ru.netologia.nmedia.dto

data class Post(
    val id: Long = 0,
    val author: String = "",
    val content: String = "",
    val published: String = "",
    val likedByMe: Boolean = false,
    val likes: Long = 0,
//    val isShare: Boolean = false,
    val shares: Long = 0,
    val views: Long = 0,
    val video: String = ""
)

