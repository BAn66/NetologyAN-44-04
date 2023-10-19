package ru.netologia.nmedia.repository

import ru.netologia.nmedia.dto.Post

interface PostRepository {
    //    fun getAll(): LiveData<List<Post>>
    fun getAll(): List<Post>
    fun getAllAsync(callback: RepositoryCallback<List<Post>>)
    fun likeById(id: Long, likedByMe: Boolean): Post
    fun likeByIdAsync(id: Long, likedByMe: Boolean, callback: RepositoryCallback<Post>)

//    fun shareById(id: Long)
    fun removeById(id: Long)
    fun removeByIdAsync(id: Long , callback: RepositoryCallback<Post>)
    fun save(post: Post): Post
    fun savegAsync(post: Post, callback: RepositoryCallback<Post>)

    fun getPostById(id: Long): Post

    interface RepositoryCallback<T> {
        fun onSuccess(result: T)
        fun onError(e: Exception)
    }

}