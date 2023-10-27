package ru.netologia.nmedia.repository

import ru.netologia.nmedia.dto.Post

interface PostRepository {
//     для retrofit

    fun getAll(): List<Post>
    fun getAllAsync(callback: GetAllCallback<List<Post>>)
    fun likeById(id: Long, likedByMe: Boolean): Post
    fun likeByIdAsync(id: Long, likedByMe: Boolean, callback: SaveCallback)
    fun removeById(id: Long)
    fun removeByIdAsync(id: Long , callback: SaveCallback)
    fun save(post: Post): Post
    fun saveAsync(post: Post, callback: GetAllCallback<Post>)
//    fun getPostById(id: Long): Post

    fun getErrMess(): Pair<Int, String>


    interface SaveCallback{
        fun onSuccess(result: Unit)
        fun onError(e: Exception)
    }

        interface GetAllCallback<T> {
        fun onSuccess(result: T)
        fun onError(e: Exception)
    }


    // для okhttp
//    //    fun getAll(): LiveData<List<Post>>
//    fun getAll(): List<Post>
//    fun getAllAsync(callback: GetAllCallback<List<Post>>)
//    fun likeById(id: Long, likedByMe: Boolean): Post
//    fun likeByIdAsync(id: Long, likedByMe: Boolean, callback: GetAllCallback<Post>)
//
////    fun shareById(id: Long)
//    fun removeById(id: Long)
//    fun removeByIdAsync(id: Long , callback: GetAllCallback<Post>)
//    fun save(post: Post): Post
//    fun saveAsync(post: Post, callback: GetAllCallback<Post>)
//    fun getPostById(id: Long): Post
//
//
//    interface GetAllCallback<T> {
//        fun onSuccess(result: T)
//        fun onError(e: Exception)
//    }
//


}