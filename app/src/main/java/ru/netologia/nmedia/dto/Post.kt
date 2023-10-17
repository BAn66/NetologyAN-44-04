package ru.netologia.nmedia.dto

data class Post(
    val id: Long = 0L,
    val author: String = "",
    val content: String = "",
    val published: Long = 0L,
    val likedByMe: Boolean = false,
    val likes: Long = 0L,
    val shares: Long = 0L,
    val views: Long = 0L,
    val video: String = ""
){
    @Override
    override fun toString(): String {
        return "id = $id , author = $author, content = $content ,published = $published, likes = $likes,likedByMe = $likedByMe, shares = $shares, views = $views"
    }
}

