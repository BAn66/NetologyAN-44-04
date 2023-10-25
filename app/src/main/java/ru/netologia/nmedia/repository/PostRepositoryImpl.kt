package ru.netologia.nmedia.repository


import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
//import okhttp3.Call
//import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
//import okhttp3.Response
import ru.netologia.nmedia.api.PostsApi
import ru.netologia.nmedia.dto.Post
import java.io.IOException
import java.util.concurrent.TimeUnit
import retrofit2.Callback
import retrofit2.Call
import retrofit2.Response

class PostRepositoryImpl : PostRepository {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val postsType: TypeToken<List<Post>> = object : TypeToken<List<Post>>() {}

    private companion object { //НЕ ЗАБУДЬ ЗАПУСТИТЬ СЕРВЕР
        //const val BASE_URL = "http://10.0.2.2:9999/api/slow/"
        const val BASE_URL = "http://10.0.2.2:9999"

        //const val BASE_URL = "http://192.168.0.57:9999"
        val jsonType = "application/json".toMediaType()
    }

    override fun getAll(): List<Post> {

        val request = Request.Builder() //запрос
            .url("${BASE_URL}/api/slow/posts")
            .build()

        val call = client.newCall(request) //сетевой вызов
        val response = call.execute() //получаем ответ
        val responseString = response.body?.string() ?: error("Body is null") //строка из ответа
        return gson.fromJson(responseString, postsType) //из строки объект листа с постами
    }

    //Асинхронная функция реализации интерфейса
    override fun getAllAsync(callback: PostRepository.GetAllCallback<List<Post>>) {

        //Через retrofit
        PostsApi.retrofitService.getAll()
            .enqueue(object : Callback<List<Post>> {
                override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
                    if (response.isSuccessful) {
                        callback.onSuccess(
                            response.body() ?: throw RuntimeException("body is empty")
                        )
                    } else {
                        callback.onError(RuntimeException("error code:${response.code()} with ${response.message()}"))
                    }
                }

                override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                    callback.onError(Exception(t))
                }
            })


        //Через okhttp
//        val request = Request.Builder() //запрос
//            .url("${BASE_URL}/api/slow/posts")
//            .build()
//
////        enqueuePostRepository(request, postsType, callback)
//        client.newCall(request)
//            .enqueue(object : Callback {
//                override fun onResponse(call: Call, response: Response) {
//                    try {
//                        val body = response.body?.string() ?: throw RuntimeException("body is null")
//                        callback.onSuccess(gson.fromJson(body, postsType))
//                    } catch (e: Exception) {
//                        callback.onError(e)
//                    }
//                }
//
//                override fun onFailure(call: Call, e: IOException) {
//                    callback.onError(e)
//                }
//            }
//            )

    }

    private fun <T> enqueuePostRepository(
        request: Request,
        typePost: Class<T>,
        callback: PostRepository.GetAllCallback<T>
    ) {
        client.newCall(request)
            .enqueue(object : okhttp3.Callback {
                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    try {
                        val result =
                            response.body?.string() ?: throw RuntimeException("body is null")
                        callback.onSuccess(gson.fromJson(result, typePost))
                    } catch (e: Exception) {
                        callback.onError(e)
                    }
                }

                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    callback.onError(e)
                }
            }
            )
    }

    //На okhhtp
    override fun save(post: Post): Post {
        val request = Request.Builder() //запрос
            .url("${BASE_URL}/api/slow/posts")
            .post(gson.toJson(post).toRequestBody(jsonType))
            .build()

        val call = client.newCall(request) //сетевой вызов
        val response = call.execute() //получаем ответ
        val responseString = response.body?.string() ?: error("Body is null") //строка из ответа
        return gson.fromJson(responseString, Post::class.java) //из строки объект листа с постами
    }

    //    На retrofit
    override fun saveAsync(post: Post, callback: PostRepository.SaveCallback) {
        PostsApi.retrofitService.save(post).enqueue(object : Callback<Post> {
            override fun onResponse(call: Call<Post>, response: Response<Post>) {
                if (response.isSuccessful) {
                    callback.onSuccess(post)
                } else {
                    callback.onError(RuntimeException("error code:${response.code()} with ${response.message()}"))
                }
            }

            override fun onFailure(call: Call<Post>, t: Throwable) {
                callback.onError(Exception(t))
            }
        })
    }

    //На okhttp
//    override fun saveAsync(post: Post, callback: PostRepository.GetAllCallback<Post>) {
//        val request = Request.Builder() //запрос
//            .url("${BASE_URL}/api/slow/posts")
//            .post(gson.toJson(post).toRequestBody(jsonType))
//            .build()
//
//        enqueuePostRepository(request, Post::class.java, callback)
//    }

    override fun likeById(id: Long, likedByMe: Boolean): Post {

        val request: Request = Request.Builder()
            .url("${BASE_URL}/api/slow/posts/$id/likes")
            .run {
                if (likedByMe) {
                    delete(gson.toJson(id).toRequestBody(jsonType))
                } else {
                    post(gson.toJson(id).toRequestBody(jsonType))
                }
            }
            .build()


        val responseString = client.newCall(request)
            .execute()
            .body?.string() ?: error("Body is null")
        return gson.fromJson(responseString, Post::class.java) //из строки объект поста
    }

    override fun likeByIdAsync(
        id: Long,
        likedByMe: Boolean,
        callback: PostRepository.GetAllCallback<Post>
    ) {
        val request: Request = Request.Builder()
            .url("${BASE_URL}/api/slow/posts/$id/likes")
            .run {
                if (likedByMe) {
                    delete(gson.toJson(id).toRequestBody(jsonType))
                } else {
                    post(gson.toJson(id).toRequestBody(jsonType))
                }
            }
            .build()

        enqueuePostRepository(request, Post::class.java, callback)

    }

    //    override fun shareById(id: Long) {
//        TODO("Not yet implemented")
//    }

    override fun removeById(id: Long) {
        val request: Request = Request.Builder()
            .delete()
            .url("${BASE_URL}/api/slow/posts/$id")
            .build()

        client.newCall(request)
            .execute()
            .close()
    }

    override fun removeByIdAsync(id: Long, callback: PostRepository.GetAllCallback<Post>) {
        val request: Request = Request.Builder()
            .delete()
            .url("${BASE_URL}/api/slow/posts/$id")
            .build()

        enqueuePostRepository(request, Post::class.java, callback)
    }

    override fun getPostById(id: Long): Post {
        val request = Request.Builder()
            .get()//запрос
            .url("${BASE_URL}/api/slow/posts/$id")
            .build()

        val call = client.newCall(request) //сетевой вызов
        val response = call.execute() //получаем ответ
        val responseString = response.body?.string() ?: error("Body is null") //строка из ответа
        return gson.fromJson(responseString, Post::class.java)
    }
}