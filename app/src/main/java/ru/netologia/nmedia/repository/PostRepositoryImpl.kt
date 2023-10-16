package ru.netologia.nmedia.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netologia.nmedia.dto.Post
import java.util.concurrent.TimeUnit

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
            .url("${BASE_URL}/api/posts")
            .build()

        val call = client.newCall(request) //сетевой вызов
        val response = call.execute() //получаем ответ
        val responseString = response.body?.string() ?: error("Body is null") //строка из ответа
        return gson.fromJson(responseString, postsType) //из строки объект листа с постами
    }

    override fun save(post: Post): Post {
        val request = Request.Builder() //запрос
            .url("${BASE_URL}/api/posts")
            .post(gson.toJson(post).toRequestBody(jsonType))
            .build()

        val call = client.newCall(request) //сетевой вызов
        val response = call.execute() //получаем ответ
        val responseString = response.body?.string() ?: error("Body is null") //строка из ответа
        return gson.fromJson(responseString, Post::class.java) //из строки объект листа с постами
    }

    override fun likeById(id: Long, likeByMe : Boolean) {

        val request: Request = Request.Builder()
            .url("${BASE_URL}/api/posts/$id/likes")
            .run {
                if (likeByMe) {
                    delete(gson.toJson(id).toRequestBody(jsonType))
                } else {
                    post(gson.toJson(id).toRequestBody(jsonType))
                }
            }
            .build()

        client.newCall(request)
            .execute()
            .close()
    }

    override fun shareById(id: Long) {
        TODO("Not yet implemented")
    }

    override fun removeById(id: Long) {
        val request: Request = Request.Builder()
            .delete()
            .url("${BASE_URL}/api/posts/$id")
            .build()

        client.newCall(request)
            .execute()
            .close()
    }


    override fun getPostById(id: Long): Post {
        val request = Request.Builder()
            .get()//запрос
            .url("${BASE_URL}/api/posts/$id")
            .build()

        val call = client.newCall(request) //сетевой вызов
        val response = call.execute() //получаем ответ
        val responseString = response.body?.string() ?: error("Body is null") //строка из ответа
        return gson.fromJson(responseString, Post::class.java)
    }
}