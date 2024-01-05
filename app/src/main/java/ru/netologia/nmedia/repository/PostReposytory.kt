package ru.netologia.nmedia.repository

//import androidx.lifecycle.LiveData

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netologia.nmedia.dto.Post
import ru.netologia.nmedia.dto.Token
import ru.netologia.nmedia.model.PhotoModel

interface PostRepository {
    //Для рума/ретрофита с корутинами добавим свойство которое будет отвечать за предоставление данных в виде LiveData
//    val data: LiveData<List<Post>> //без flow
//    val data: Flow<List<Post>>// c Flow до Paging
    val data: Flow<PagingData<Post>>
    fun getNewer(id: Long): Flow<Int>
    suspend fun switchNewOnShowed(): Boolean
    suspend fun getAll()
    suspend fun save(post: Post)
    suspend fun removeById(id: Long)
    suspend fun likeById(id: Long, likedByMe: Boolean)
    // suspend fun getPostById(id: Long)
    fun getErrMess(): Pair<Int, String>
    suspend fun saveWithAttachment(postCopy: Post, photoModel: PhotoModel)
    suspend fun requestToken(login: String, password: String): Token

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
