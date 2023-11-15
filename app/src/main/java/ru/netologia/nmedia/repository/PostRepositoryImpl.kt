package ru.netologia.nmedia.repository

import ru.netologia.nmedia.api.PostsApi
import ru.netologia.nmedia.dto.Post
import java.io.IOException
import androidx.lifecycle.map
import ru.netologia.nmedia.dao.PostDao
import ru.netologia.nmedia.entity.PostEntity
import ru.netologia.nmedia.entity.toDto
import ru.netologia.nmedia.entity.toEntity
import ru.netologia.nmedia.error.*


class PostRepositoryImpl(private val dao: PostDao) : PostRepository {
    override val data = dao.getAll().map(List<PostEntity>::toDto) //Берем текущую локальную БД

    private var responseErrMess: Pair<Int, String> = Pair(0, "")
    override fun getErrMess(): Pair<Int, String> {
        return responseErrMess
    }


    override suspend fun getAll() {
        try {
            val response = PostsApi.retrofitService.getAll()
            if (!response.isSuccessful) {
                responseErrMess = Pair(response.code(), response.message())
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(body.toEntity()) // А вот здесь в Локальную БД вставляем из сети все посты
        } catch (e: IOException) {
            responseErrMess = Pair(NetworkError.code.toInt(), NetworkError.message.toString())
            throw NetworkError

        } catch (e: Exception) {
            responseErrMess = Pair(UnknownError.code.toInt(), UnknownError.message.toString())
            throw UnknownError
        }
    }

    override suspend fun save(post: Post) {
        try {

            val response = PostsApi.retrofitService.save(post)
            if (!response.isSuccessful) {
                responseErrMess = Pair(response.code(), response.message())
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(PostEntity.fromDto(body))
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
//            //Не рабочий вариант
//            //1. Делаем копию поста из БД, меняем в ней лайк на тру/фолс из парметров метода и снова записываем его в базу
//            // где по идее он должен перезаписаться НО НЕ ПЕРЕЗАПИСЫВАЕТСЯ (почему?)
//            val post = dao.getPostById(id).toDto().copy(likedByMe = likedByMe)
//            dao.insert(PostEntity.fromDto(post))
//            //2. Далее меням лайк на сервере через ретрофит
//            val response =
//                PostsApi.retrofitService.let { if (likedByMe) it.dislikeById(id) else it.likeById(id) }
//            if (!response.isSuccessful) {
//                responseErrMess = Pair(response.code(), response.message())
//                throw ApiError(response.code(), response.message())
//            }
//            val body = response.body() ?: throw ApiError(response.code(), response.message())
//
//            //ВОПРОС! почему в локальной БД ничего не изменяется, судя по инспектору

            //Рабочий вариант
            val response =
                PostsApi.retrofitService.let { if (likedByMe) it.dislikeById(id) else it.likeById(id) }
            if (!response.isSuccessful) {
                responseErrMess = Pair(response.code(), response.message())
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(PostEntity.fromDto(body))

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
            val response = PostsApi.retrofitService.removeById(id)
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