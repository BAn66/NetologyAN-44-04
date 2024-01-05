package ru.netologia.nmedia.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netologia.nmedia.dto.Post
import ru.netologia.nmedia.dto.Token
import ru.netologia.nmedia.model.PhotoModel

interface PostRepository {
    val data: Flow<PagingData<Post>>
    fun getNewer(id: Long): Flow<Int>
    suspend fun switchNewOnShowed(): Boolean
    suspend fun getAll()
    suspend fun save(post: Post)
    suspend fun removeById(id: Long)
    suspend fun likeById(id: Long, likedByMe: Boolean)
    fun getErrMess(): Pair<Int, String>
    suspend fun saveWithAttachment(postCopy: Post, photoModel: PhotoModel)
    suspend fun requestToken(login: String, password: String): Token
}
