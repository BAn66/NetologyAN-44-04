package ru.netologia.nmedia.repository

import androidx.lifecycle.asLiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
//import ru.netologia.nmedia.api.PostsApi
import ru.netologia.nmedia.dto.Post
import java.io.IOException
//import androidx.lifecycle.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import ru.netologia.nmedia.api.ApiService
//import retrofit2.http.Multipart
//import okhttp3.Dispatcher
import ru.netologia.nmedia.dao.PostDao
import ru.netologia.nmedia.dto.Attachment
import ru.netologia.nmedia.dto.Token
import ru.netologia.nmedia.entity.PostEntity
import ru.netologia.nmedia.entity.toEntity
import ru.netologia.nmedia.enumeration.AttachmentType
import ru.netologia.nmedia.error.*
import ru.netologia.nmedia.model.PhotoModel
import ru.netology.nmedia.dto.Media
import java.io.File
import javax.inject.Inject


class PostRepositoryImpl @Inject constructor(
    private val dao: PostDao,
    private val apiService: ApiService
) : PostRepository {

    //плэйсхолдеры отключены для упрощения демонстрации Paging
    override val data = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = false),
        pagingSourceFactory = {
            PostPagingSource(apiService)
        }
    ).flow

    //Для отслеживания кодов ошибок
    private var responseErrMess: Pair<Int, String> = Pair(0, "")
    override fun getErrMess(): Pair<Int, String> {
        return responseErrMess
    }


    override suspend fun getAll() {
        try {
            saveOnServerCheck() //проверка текущий локальной БД на незаписанные посты на сервер, если такие есть то они пытаются отправится на сервер через save()
            val response = apiService.getAll()
            if (!response.isSuccessful) {
                responseErrMess = Pair(response.code(), response.message())
                throw ApiError(response.code(), response.message())
            }
            val bodyRsponse = response.body() ?: throw ApiError(response.code(), response.message())
            val entityList = bodyRsponse.toEntity() //Превращаем ответ в лист с энтити

            dao.insert(entityList)// А вот здесь в Локальную БД вставляем из сети все посты
            //А тут всем постам пришедшим с сервера ставим отметку тру
            for (postEntity: PostEntity in entityList) {
                if (!postEntity.savedOnServer) {
                    dao.saveOnServerSwitch(postEntity.id)
                }
            }

        } catch (e: IOException) {
//            e.printStackTrace()
            responseErrMess = Pair(NetworkError.code.toInt(), NetworkError.message.toString())
            throw NetworkError

        } catch (e: Exception) {
//            e.printStackTrace()
            responseErrMess = Pair(UnknownError.code.toInt(), UnknownError.message.toString())
            throw UnknownError
        }
    }

    override fun getNewer(id: Long): Flow<Int> = flow {
        while (true) {
            delay(10_000L)
            val response = apiService.getNewer(id)
            if (!response.isSuccessful) {
                responseErrMess = Pair(response.code(), response.message())
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(
                body.toEntity().map {
                    it.copy(
                        savedOnServer = true,
                        showed = false
                    )
                })//вставляем в базу, скопированный ответ с сервера с нужными нам маркерами, записан на сервере и не показывать.
            emit(body.size)
        }
    }
        .catch { throw UnknownError } //Репозиторий может выбрасывать исключения, но их тогда нужно обрабатывать во вьюмодели, тоже в кэтче флоу


    override suspend fun switchNewOnShowed(): Boolean {
        dao.showedSwitch()
        return false
    }


    override suspend fun save(post: Post) {
        try {
            //Запись сначала в БД.
            val postEntentety = PostEntity.fromDto(post)
            dao.insert(postEntentety) //при сохранении поста, в базу вносится интентети с отметкой что оно не сохарнено на сервере
            val response =
                apiService.save(post.copy(id = 0)) //Если у поста айди 0 то сервер воспринимает его как новый
            if (!response.isSuccessful) { //если отвтет с сервера не пришел, то отметка о не записи на сервер по прежнему фолс
                responseErrMess = Pair(response.code(), response.message())
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.saveOnServerSwitch(body.id)// исключение не брошено меняем отметку о записи на сервере на тру

        } catch (e: IOException) {
            responseErrMess = Pair(NetworkError.code.toInt(), NetworkError.message.toString())
            throw NetworkError
        } catch (e: Exception) {
            responseErrMess = Pair(UnknownError.code.toInt(), UnknownError.message.toString())
            throw UnknownError
        }
        getAll()
    }

    suspend fun saveOnServerCheck() {
        try {
            for (postEntity: PostEntity in dao.getAll()
                .asLiveData(Dispatchers.Default)
                .value ?: emptyList()
            ) {
                if (!postEntity.savedOnServer) {
                    save(postEntity.toDto())
                }
            }
        } catch (e: IOException) {
            responseErrMess = Pair(NetworkError.code.toInt(), NetworkError.message.toString())
            throw NetworkError
        } catch (e: Exception) {
            responseErrMess = Pair(UnknownError.code.toInt(), UnknownError.message.toString())
            throw UnknownError
        }
    }

    override suspend fun likeById(id: Long, likedByMe: Boolean) {
        try {
            dao.likeById(id)
            val response =
                apiService.let { if (likedByMe) it.dislikeById(id) else it.likeById(id) }
            if (!response.isSuccessful) {
                responseErrMess = Pair(response.code(), response.message())
                throw ApiError(response.code(), response.message())
            }
            response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            responseErrMess = Pair(NetworkError.code.toInt(), NetworkError.message.toString())
            throw NetworkError

        } catch (e: Exception) {
            responseErrMess = Pair(UnknownError.code.toInt(), UnknownError.message.toString())
            throw UnknownError
        }
    }


    override suspend fun removeById(id: Long) {
        try {
            dao.removeById(id)
            val response = apiService.removeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            responseErrMess = Pair(NetworkError.code.toInt(), NetworkError.message.toString())
            throw NetworkError

        } catch (e: Exception) {
            responseErrMess = Pair(UnknownError.code.toInt(), UnknownError.message.toString())
            throw UnknownError
        }

    }

    override suspend fun saveWithAttachment(postCopy: Post, photoModel: PhotoModel) {
        try {

            val mediaResponse = saveMediaOnServer(photoModel.file)
            if (!mediaResponse.isSuccessful) {
                responseErrMess = Pair(mediaResponse.code(), mediaResponse.message())
                throw ApiError(mediaResponse.code(), mediaResponse.message())
            }
            val media = mediaResponse.body() ?: throw ApiError(
                mediaResponse.code(),
                mediaResponse.message()
            )

            val postEntentety = PostEntity.fromDto(postCopy)
            dao.insert(postEntentety)

            val response = apiService.save(
                postCopy.copy(
                    id = 0,
                    attachment = Attachment(
                        media.id,
                        AttachmentType.IMAGE
                    )
                )
            )

            if (!response.isSuccessful) {
                responseErrMess = Pair(response.code(), response.message())
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.saveOnServerSwitch(body.id)

        } catch (e: IOException) {
            responseErrMess = Pair(NetworkError.code.toInt(), NetworkError.message.toString())
            throw NetworkError
        } catch (e: Exception) {
            responseErrMess = Pair(UnknownError.code.toInt(), UnknownError.message.toString())
            throw UnknownError
        }
        getAll()
    }

    private suspend fun saveMediaOnServer(file: File): Response<Media> {
        val part = MultipartBody.Part.createFormData("file", file.name, file.asRequestBody())
        return apiService.saveMediaOnServer(part)
    }

    override suspend fun requestToken(login: String, password: String): Token {
        try {
            val response = apiService.updateUser(login, password)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            return body

        } catch (e: IOException) {
            responseErrMess = Pair(NetworkError.code.toInt(), NetworkError.message.toString())
            throw NetworkError

        } catch (e: Exception) {
            responseErrMess = Pair(UnknownError.code.toInt(), UnknownError.message.toString())
            throw UnknownError
        }
    }
}
