package ru.netologia.nmedia.api


import com.google.firebase.ktx.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import ru.netologia.nmedia.dto.Post


private const val BASE_URL = "http://10.0.2.2:9999/api/slow/"
//private const val BASE_URL = "${BuildConfig.}/api/slow/"

interface PostsApiService {
    @GET("posts")
    fun getAll(): Call<List<Post>>
    //    fun getAllAsync(callback: PostRepository.RepositoryCallback<List<Post>>)

    @POST("posts/{id}/likes")
    fun likeById(@Path("id") id: Long, @Path("likedByMe") likedByMe: Boolean): Call<Post>
    //    fun likeByIdAsync(id: Long, likedByMe: Boolean, callback: PostRepository.RepositoryCallback<Post>)

    //    fun shareById(id: Long)

    @POST("posts")
    fun save(@Body post: Post): Call<Post>
//    fun saveAsync(post: Post, callback: PostRepository.SaveCallback)

    @DELETE("posts/{id}")
    fun removeById(@Path("id")id: Long): Call<Unit>
    //    fun removeByIdAsync(id: Long , callback: PostRepository.RepositoryCallback<Post>)

    fun getPostById(id: Long): Post
}

//логгер
val logger = HttpLoggingInterceptor().apply {
    if (BuildConfig.DEBUG){
        level = HttpLoggingInterceptor.Level.BODY
    }
}
val client = OkHttpClient.Builder().addInterceptor(logger).build()


val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .client(client)
    .addConverterFactory(GsonConverterFactory.create())
    .build()

object PostsApi {
    val retrofitService by lazy {
        retrofit.create(PostsApiService::class.java)
    }
}