package ru.netologia.nmedia.repository

import androidx.lifecycle.asLiveData
//import ru.netologia.nmedia.api.PostsApi
import ru.netologia.nmedia.dto.Post
import java.io.IOException
//import androidx.lifecycle.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import ru.netologia.nmedia.api.PostsApiService
//import retrofit2.http.Multipart
//import okhttp3.Dispatcher
import ru.netologia.nmedia.dao.PostDao
import ru.netologia.nmedia.dto.Attachment
import ru.netologia.nmedia.dto.Token
import ru.netologia.nmedia.entity.PostEntity
import ru.netologia.nmedia.entity.toDto
import ru.netologia.nmedia.entity.toEntity
import ru.netologia.nmedia.enumeration.AttachmentType
import ru.netologia.nmedia.error.*
import ru.netologia.nmedia.model.PhotoModel
import ru.netology.nmedia.dto.Media
import java.io.File


class PostRepositoryImpl(
    private val dao: PostDao,
    private val apiService: PostsApiService //Заменяем этим аписервисом, все вызовы PostsApi.retrofitService
) : PostRepository {
    override val data = dao.getAll()
        .map(List<PostEntity>::toDto) //Берем текущую локальную БД
    //        .flowOn(Dispatchers.Default) //вызывются не на главном потоке//но мы это сделаем в поствьюмодел


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
//        .flowOn(Dispatchers.Default)

    override suspend fun haveNewer(): Boolean {
        dao.showedSwitch()
        return false
    }


    override suspend fun save(post: Post) {
        try {

            //Рабочий вариант с записью сначала на сервер
//            val response = PostsApi.retrofitService.save(post)
//            if (!response.isSuccessful) {
//                responseErrMess = Pair(response.code(), response.message())
//                throw ApiError(response.code(), response.message())
//            }
//            val body = response.body() ?: throw ApiError(response.code(), response.message())
//            dao.insert(PostEntity.fromDto(body))

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


////Версия без корутин и рум
//class PostRepositoryImpl : PostRepository {
//    private val client = OkHttpClient.Builder()
//        .connectTimeout(30, TimeUnit.SECONDS)
//        .build()
//
//    private val gson = Gson()
//    private val postsType: TypeToken<List<Post>> = object : TypeToken<List<Post>>() {}
//    private var responseErrMess : Pair<Int, String> = Pair(0, "")
//
//    private companion object { //НЕ ЗАБУДЬ ЗАПУСТИТЬ СЕРВЕР
//        //        const val BASE_URL = "http://10.0.2.2:9999/api/slow/"
//        const val BASE_URL = "http://10.0.2.2:9999"
//
//        //const val BASE_URL = "http://192.168.0.57:9999"
//        val jsonType = "application/json".toMediaType()
//    }
//
//    override fun getErrMess(): Pair<Int, String>{
//        return responseErrMess
//    }
//
//    override fun getAll(): List<Post> {
//
//        val request = Request.Builder() //запрос
//            .url("${BASE_URL}/api/slow/posts")
//            .build()
//
//        val call = client.newCall(request) //сетевой вызов
//        val response = call.execute() //получаем ответ
//        val responseString = response.body?.string() ?: error("Body is null") //строка из ответа
//        return gson.fromJson(responseString, postsType) //из строки объект листа с постами
//    }
//
//    //Асинхронная функция реализации интерфейса
//    override fun getAllAsync(callback: PostRepository.GetAllCallback<List<Post>>) {
//        //Через retrofit при положительных ответах
//        PostsApi.retrofitService.getAll()
//            .enqueue(object : Callback<List<Post>> {
//                override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
//                    if (response.isSuccessful) {
//                        callback.onSuccess(
//                            response.body() ?: throw RuntimeException("body is empty")
//                        )
//                    } else {
//                        responseErrMess = Pair(response.code(), response.message())
//                        callback.onError(RuntimeException("error code:${response.code()} with ${response.message()}"))
//                    }
//                }
//
//                override fun onFailure(call: Call<List<Post>>, t: Throwable) {
//                    callback.onError(Exception(t))
//                }
//            })
//
////        //Через retrofit при положительных ответах
////        PostsApi.retrofitService.getAll()
////            .enqueue(object : Callback<List<Post>> {
////                override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
////                    if (response.isSuccessful) {
////                        callback.onSuccess(
////                            response.body() ?: throw RuntimeException("body is empty")
////                        )
////                    } else {
////                        callback.onError(RuntimeException("error code:${response.code()} with ${response.message()}"))
////                    }
////                }
////
////                override fun onFailure(call: Call<List<Post>>, t: Throwable) {
////                    callback.onError(Exception(t))
////                }
////            })
//
//
//        //Через okhttp
////        val request = Request.Builder() //запрос
////            .url("${BASE_URL}/api/slow/posts")
////            .build()
////
//////        enqueuePostRepository(request, postsType, callback)
////        client.newCall(request)
////            .enqueue(object : Callback {
////                override fun onResponse(call: Call, response: Response) {
////                    try {
////                        val body = response.body?.string() ?: throw RuntimeException("body is null")
////                        callback.onSuccess(gson.fromJson(body, postsType))
////                    } catch (e: Exception) {
////                        callback.onError(e)
////                    }
////                }
////
////                override fun onFailure(call: Call, e: IOException) {
////                    callback.onError(e)
////                }
////            }
////            )
//
//    }
//
//
//    private fun <T> enqueuePostRepository(
//        request: Request,
//        typePost: Class<T>,
//        callback: PostRepository.GetAllCallback<T>
//    ) {
//        client.newCall(request)
//            .enqueue(object : okhttp3.Callback {
//                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
//                    try {
//                        val result =
//                            response.body?.string() ?: throw RuntimeException("body is null")
//                        callback.onSuccess(gson.fromJson(result, typePost))
//                    } catch (e: Exception) {
//                        callback.onError(e)
//                    }
//                }
//
//                override fun onFailure(call: okhttp3.Call, e: IOException) {
//                    callback.onError(e)
//                }
//            }
//            )
//    }
//
//    override fun save(post: Post): Post {
//        val request = Request.Builder() //запрос
//            .url("${BASE_URL}/api/slow/posts")
//            .post(gson.toJson(post).toRequestBody(jsonType))
//            .build()
//
//        val call = client.newCall(request) //сетевой вызов
//        val response = call.execute() //получаем ответ
//        val responseString = response.body?.string() ?: error("Body is null") //строка из ответа
//        return gson.fromJson(responseString, Post::class.java) //из строки объект листа с постами
//    }
//
//    //    На retrofit
//    override fun saveAsync(post: Post, callback: PostRepository.GetAllCallback<Post>) {
//        PostsApi.retrofitService.save(post).enqueue(object : Callback<Post> {
//            override fun onResponse(call: Call<Post>, response: Response<Post>) {
//                if (response.isSuccessful) {
//                    callback.onSuccess(post)
//                } else {
//                    callback.onError(RuntimeException("error code:${response.code()} with ${response.message()}"))
//                }
//            }
//
//            override fun onFailure(call: Call<Post>, t: Throwable) {
//                callback.onError(Exception(t))
//            }
//        })
//    }
//
//    //На okhttp
////    override fun saveAsync(post: Post, callback: PostRepository.GetAllCallback<Post>) {
////        val request = Request.Builder() //запрос
////            .url("${BASE_URL}/api/slow/posts")
////            .post(gson.toJson(post).toRequestBody(jsonType))
////            .build()
////
////        enqueuePostRepository(request, Post::class.java, callback)
////    }
//
//    override fun likeById(id: Long, likedByMe: Boolean): Post {
//
//        val request: Request = Request.Builder()
//            .url("${BASE_URL}/api/slow/posts/$id/likes")
//            .run {
//                if (likedByMe) {
//                    delete(gson.toJson(id).toRequestBody(jsonType))
//                } else {
//                    post(gson.toJson(id).toRequestBody(jsonType))
//                }
//            }
//            .build()
//
//
//        val responseString = client.newCall(request)
//            .execute()
//            .body?.string() ?: error("Body is null")
//        return gson.fromJson(responseString, Post::class.java) //из строки объект поста
//    }
//
//    override fun likeByIdAsync(
//        id: Long,
//        likedByMe: Boolean,
//        callback: PostRepository.SaveCallback
//    ) {
//        if (likedByMe) {
//            PostsApi.retrofitService.likeByIdDelete(id).enqueue(object : Callback<Post> {
//                override fun onResponse(call: Call<Post>, response: Response<Post>) {
//                    if (response.isSuccessful) {
//                        callback.onSuccess(Unit)
//                    } else {
//                        callback.onError(RuntimeException("error code:${response.code()} with ${response.message()}"))
//                    }
//                }
//
//                override fun onFailure(call: Call<Post>, t: Throwable) {
//                    callback.onError(Exception(t))
//                }
//
//            })
//        } else {
//            PostsApi.retrofitService.likeByIdPost(id).enqueue(object : Callback<Post> {
//                override fun onResponse(call: Call<Post>, response: Response<Post>) {
//                    if (response.isSuccessful) {
//                        callback.onSuccess(Unit)
//                    } else {
//                        callback.onError(RuntimeException("error code:${response.code()} with ${response.message()}"))
//                    }
//                }
//
//                override fun onFailure(call: Call<Post>, t: Throwable) {
//                    callback.onError(Exception(t))
//                }
//
//            })
//        }
//
////        val request: Request = Request.Builder()
////            .url("${BASE_URL}/api/slow/posts/$id/likes")
////            .run {
////                if (likedByMe) {
////                    delete(gson.toJson(id).toRequestBody(jsonType))
////                } else {
////                    post(gson.toJson(id).toRequestBody(jsonType))
////                }
////            }
////            .build()
////
////        enqueuePostRepository(request, Post::class.java, callback)
//
//    }
//
//    //    override fun shareById(id: Long) {
////        TODO("Not yet implemented")
////    }
//
//    override fun removeById(id: Long) {
//        val request: Request = Request.Builder()
//            .delete()
//            .url("${BASE_URL}/api/slow/posts/$id")
//            .build()
//
//        client.newCall(request)
//            .execute()
//            .close()
//    }
//
//    override fun removeByIdAsync(id: Long, callback: PostRepository.SaveCallback) {
//
//        PostsApi.retrofitService.removeById(id).enqueue(object : Callback<Unit> {
//            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
//                if (response.isSuccessful) {
//                    callback.onSuccess(Unit)
//                } else {
//                    callback.onError(RuntimeException("error code:${response.code()} with ${response.message()}"))
//                }
//            }
//
//            override fun onFailure(call: Call<Unit>, t: Throwable) {
//                callback.onError(Exception(t))
//            }
//
//        })
//
////        val request: Request = Request.Builder()
////            .delete()
////            .url("${BASE_URL}/api/slow/posts/$id")
////            .build()
////
////        enqueuePostRepository(request, Post::class.java, callback)
//    }
//
////    override fun getPostById(id: Long): Post {
////        val request = Request.Builder()
////            .get()//запрос
////            .url("${BASE_URL}/api/slow/posts/$id")
////            .build()
////
////        val call = client.newCall(request) //сетевой вызов
////        val response = call.execute() //получаем ответ
////        val responseString = response.body?.string() ?: error("Body is null") //строка из ответа
////        return gson.fromJson(responseString, Post::class.java)
////    }

//    override fun getPostById(id: Long): Post {
//        val request = Request.Builder()
//            .get()//запрос
//            .url("${BASE_URL}/api/slow/posts/$id")
//            .build()
//
//        val call = client.newCall(request) //сетевой вызов
//        val response = call.execute() //получаем ответ
//        val responseString = response.body?.string() ?: error("Body is null") //строка из ответа
//        return gson.fromJson(responseString, Post::class.java)
//    }