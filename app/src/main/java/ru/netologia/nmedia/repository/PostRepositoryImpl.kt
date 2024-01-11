package ru.netologia.nmedia.repository

import androidx.lifecycle.asLiveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.insertSeparators
import androidx.paging.map

import ru.netologia.nmedia.dto.Post
import java.io.IOException

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import ru.netologia.nmedia.api.ApiService
import ru.netologia.nmedia.dao.PostDao
import ru.netologia.nmedia.dao.PostRemoteKeyDao
import ru.netologia.nmedia.db.AppDb
import ru.netologia.nmedia.dto.Ad
import ru.netologia.nmedia.dto.Attachment
import ru.netologia.nmedia.dto.FeedItem
import ru.netologia.nmedia.dto.Token
import ru.netologia.nmedia.entity.PostEntity
import ru.netologia.nmedia.entity.toEntity
import ru.netologia.nmedia.enumeration.AttachmentType
import ru.netologia.nmedia.model.PhotoModel
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import java.io.File
import java.util.Random
import javax.inject.Inject


class PostRepositoryImpl @Inject constructor(
    private val postDao: PostDao,
    private val apiService: ApiService,
    postRemoteKeyDao: PostRemoteKeyDao,
    appDb: AppDb
) : PostRepository {

    //плэйсхолдеры отключены для упрощения демонстрации Paging
    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<FeedItem>> = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = true),
        pagingSourceFactory = { postDao.getPagingSource() },
        remoteMediator = PostRemoteMediator(
            apiService = apiService,
            postDao = postDao,
            postRemoteKeyDao = postRemoteKeyDao,
            appDb = appDb,
        )
    ).flow
        .map { pagingData ->
            pagingData.map(PostEntity::toDto)
                .insertSeparators { previous, _ ->  //Реализация вставки рекламы. Динамическое появление рекламы
                    if (previous?.id?.rem(5) == 0L){
                        Ad(Random().nextLong(), "figma.jpg")
                    } else {
                        null
                    }
                }
        }

    override val newerPostId: Flow<Long?> = postDao.max()

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

            postDao.insert(entityList)// А вот здесь в Локальную БД вставляем из сети все посты
            //А тут всем постам пришедшим с сервера ставим отметку тру
            for (postEntity: PostEntity in entityList) {
                if (!postEntity.savedOnServer) {
                    postDao.saveOnServerSwitch(postEntity.id)
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

//    override fun getAfter(id: Long): Flow<Int> {}

    override fun getNewer(id: Long): Flow<Int> = flow {
        while (true) {
            delay(10_000L)
            val response = apiService.getNewer(id)
            if (!response.isSuccessful) {
                responseErrMess = Pair(response.code(), response.message())
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(
                body.toEntity().map {
                    it.copy(
                        savedOnServer = true,
                        showed = false
                    )
                })//вставляем в базу, скопированный ответ с сервера с нужными нам маркерами, записан на сервере и не показывать.
            emit(body.size)
        }
    }
        .catch { throw AppError.from(it) } //Репозиторий может выбрасывать исключения, но их тогда нужно обрабатывать во вьюмодели, тоже в кэтче флоу

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getNewerCount(): Flow<Long> = postDao.max()
        .flatMapLatest {
            if (it != null) {
                flow {
                    while (true) {
                        delay(10_000L)
                        val response = apiService.getNewerCount(it)
                        val body = response.body()
                        emit(body?.count ?: 0)
                    }
                }
            } else {
                emptyFlow()
            }
        }
        .catch { e ->
            throw AppError.from(e)
        }

    override suspend fun switchNewOnShowed(): Boolean {
        postDao.showedSwitch()
        return false
    }


    override suspend fun save(post: Post) {
        try {
            //Запись сначала в БД.
            val postEntentety = PostEntity.fromDto(post)
            postDao.insert(postEntentety) //при сохранении поста, в базу вносится интентети с отметкой что оно не сохарнено на сервере
            val response =
                apiService.save(post.copy(id = 0)) //Если у поста айди 0 то сервер воспринимает его как новый
            if (!response.isSuccessful) { //если отвтет с сервера не пришел, то отметка о не записи на сервер по прежнему фолс
                responseErrMess = Pair(response.code(), response.message())
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.saveOnServerSwitch(body.id)// исключение не брошено меняем отметку о записи на сервере на тру

        } catch (e: IOException) {
            responseErrMess = Pair(NetworkError.code.toInt(), NetworkError.message.toString())
            throw NetworkError
        } catch (e: Exception) {
            responseErrMess = Pair(UnknownError.code.toInt(), UnknownError.message.toString())
            throw UnknownError
        }
//        getAll()
    }

    suspend fun saveOnServerCheck() {
        try {
            for (postEntity: PostEntity in postDao.getAllFromDb()
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
            postDao.likeById(id)
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
            postDao.removeById(id)
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
            postDao.insert(postEntentety)

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
            postDao.saveOnServerSwitch(body.id)

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
