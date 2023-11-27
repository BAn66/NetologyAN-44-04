package ru.netologia.nmedia.repository

import androidx.lifecycle.LiveData
import ru.netologia.nmedia.dto.Post

interface PostRepository {
    //Для рума/ретрофита с корутинами добавим свойство которое будет отвечать за предоставление данных в виде LiveData
    val data: LiveData<List<Post>>
    suspend fun getAll()
    suspend fun save(post: Post)
    suspend fun removeById(id: Long)
    suspend fun likeById(id: Long, likedByMe: Boolean)
    // suspend fun getPostById(id: Long)
    fun getErrMess(): Pair<Int, String>
}

 //для ретрофита без всего
// interface PostRepository {

//     //     для retrofit
//     suspend fun getAll(): List<Post>
//     fun getAllAsync(callback: GetAllCallback<List<Post>>)
//     fun save(post: Post): Post
//     fun saveAsync(post: Post, callback: GetAllCallback<Post>)
//     fun removeById(id: Long)
//
//     fun removeByIdAsync(id: Long , callback: SaveCallback)
//     fun likeById(id: Long, likedByMe: Boolean): Post
//     fun likeByIdAsync(id: Long, likedByMe: Boolean, callback: SaveCallback)
////    fun getPostById(id: Long): Post
//
//     fun getErrMess(): Pair<Int, String>
//
//
//     interface SaveCallback {
//         fun onSuccess(result: Unit)
//         fun onError(e: Exception)
//     }
//
//     interface GetAllCallback<T> {
//         fun onSuccess(result: T)
//         fun onError(e: Exception)
//     }
// }


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
