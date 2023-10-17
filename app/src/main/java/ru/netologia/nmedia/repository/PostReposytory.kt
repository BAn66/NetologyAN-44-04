package ru.netologia.nmedia.repository

import ru.netologia.nmedia.dto.Post

interface PostRepository {
//    fun getAll(): LiveData<List<Post>>
    fun getAll(): List<Post>
    fun likeById(id: Long, likedByMe: Boolean): Post
    fun shareById(id: Long)
    fun removeById(id: Long)
    fun save(post: Post): Post
    fun getPostById(id: Long): Post

}