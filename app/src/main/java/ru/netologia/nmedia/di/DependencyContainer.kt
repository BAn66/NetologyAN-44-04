package ru.netologia.nmedia.di

import android.annotation.SuppressLint
import android.content.Context
import androidx.room.Room
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import ru.netologia.nmedia.BuildConfig
import ru.netologia.nmedia.api.PostsApiService
import ru.netologia.nmedia.auth.AppAuth
import ru.netologia.nmedia.db.AppDb
import ru.netologia.nmedia.repository.PostRepository
import ru.netologia.nmedia.repository.PostRepositoryImpl

class DependencyContainer(
    private val context: Context
) {
    //Переменные из PostApi перенесены сюда
    companion object {
        private const val BASE_URL = "${BuildConfig.BASE_URL}/api/slow/"


        @Volatile
        private var instance: DependencyContainer? = null

        fun initApp(context: Context){
            instance = DependencyContainer(context)
        }

        fun getInstance(): DependencyContainer {
            return instance!!
        }

        private fun buildDatabase(context: Context) =
                Room.databaseBuilder(context, AppDb::class.java, "app.db")
//                .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build()
    }

    val appAuth = AppAuth(context)

    //Перехватчик для логгера
    val logger = HttpLoggingInterceptor().apply {
        if (BuildConfig.DEBUG) {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }
    //клиент OkHttp
    val clientOkHttp = OkHttpClient.Builder()
        .addInterceptor{ chain ->
//            AppAuth.getInstance().authState.value.token?.let { token->
            appAuth.authState.value.token?.let { token->
                val newRequest = chain.request().newBuilder()
                    .addHeader("Authorization", token)
                    .build()
                return@addInterceptor chain.proceed(newRequest)
            }
            chain.proceed(chain.request())
        }
        .addInterceptor(logger)
        .build()

    // и ретрофит
    val retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .client(clientOkHttp)
        .baseUrl(BASE_URL)
        .build()

    private val appBd = Room.databaseBuilder(context, AppDb::class.java, "app.db")
//                .allowMainThreadQueries()
    .fallbackToDestructiveMigration()
    .build()

    val apiService = retrofit.create<PostsApiService>()

    private val postDao = appBd.postDao()

    private val repository: PostRepository = PostRepositoryImpl(
        postDao,
        apiService,
        )



}