package ru.netologia.nmedia.repository

import androidx.lifecycle.LiveData
import ru.netologia.nmedia.dto.Post

interface PostRepository {
    fun getAll(): LiveData<List<Post>>
    fun likeById(id: Long)
    fun shareById(id: Long)
    fun removeById(id: Long)
    fun save(post: Post)
    fun getPostById(id: Long): Post

}