package ru.netologia.nmedia.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.tasks.await
import ru.netologia.nmedia.api.ApiService
//import ru.netologia.nmedia.di.DependencyContainer
//import ru.netologia.nmedia.api.PostsApi
import ru.netologia.nmedia.dto.PushToken

class SendPushTokenWorker(
    @ApplicationContext
    private val applicationContext: Context,
    params: WorkerParameters
): CoroutineWorker(applicationContext, params) {
    companion object {
        const val NAME = "SendPushTokenWorker"
        const val TOKEN_KEY = "TOKEN_KEY"
    }

    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface AppAuthEntryPoint{ //Не стандартный способ добрать до аписервиса из зависимостей HILTa
        fun getApiService(): ApiService
    }

    override suspend fun doWork(): Result =
        try {
            val token = inputData.getString(TOKEN_KEY)

            val pushToken = PushToken(token ?: FirebaseMessaging.getInstance().token.await())
            //           DependencyContainer.getInstance().apiService.sendPushToken(pushToken) //берем из контейнера зависимостей
           val entryPoint =  EntryPointAccessors.fromApplication(applicationContext, AppAuthEntryPoint::class.java) //берем из HILT
            entryPoint.getApiService().sendPushToken(pushToken)
            Result.success()
        } catch (e: Exception){
            e.printStackTrace()
            Result.retry()
        }

}