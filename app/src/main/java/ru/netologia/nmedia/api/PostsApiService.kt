package ru.netologia.nmedia.api



import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import ru.netologia.nmedia.BuildConfig
import ru.netologia.nmedia.dto.Post


//private const val BASE_URL = "http://10.0.2.2:9999/api/slow/"
private const val BASE_URL = "${BuildConfig.BASE_URL}/api/slow/"
//логгер и ретрофит
val logger = HttpLoggingInterceptor().apply {
    if (BuildConfig.DEBUG){
        level = HttpLoggingInterceptor.Level.BODY
    }
}
val clientOkHttp  = OkHttpClient.Builder().addInterceptor(logger).build()

val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .client(clientOkHttp)
    .addConverterFactory(GsonConverterFactory.create())
    .build()

interface PostsApiService {
    @GET("posts")
    suspend fun getAll(): Response<List<Post>>

    @GET("posts/{id}")
    suspend fun getById(@Path("id") id: Long): Response<Post>

    @POST("posts")
    suspend fun save(@Body post: Post): Response<Post>

    @DELETE("posts/{id}")
    suspend fun removeById(@Path("id")id: Long): Response<Unit>

    @POST("posts/{id}/likes")
    suspend fun likeById(@Path("id") id: Long): Response<Post>

    @DELETE("posts/{id}/likes")
    suspend fun dislikeById(@Path("id") id: Long): Response<Post>

}

object PostsApi {
    val retrofitService by lazy {
        retrofit.create(PostsApiService::class.java)
    }
}