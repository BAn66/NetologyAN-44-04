package ru.netologia.nmedia.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import ru.netologia.nmedia.di.DependencyContainer
//import ru.netologia.nmedia.api.PostsApi
import ru.netologia.nmedia.dto.PushToken

class SendPushTokenWorker(
    applicationContext: Context,
    params: WorkerParameters
): CoroutineWorker(applicationContext, params) {
    companion object {
        const val NAME = "SendPushTokenWorker"
        const val TOKEN_KEY = "TOKEN_KEY"
    }

    override suspend fun doWork(): Result =
        try {
            val token = inputData.getString(TOKEN_KEY)

            val pushToken = PushToken(token ?: FirebaseMessaging.getInstance().token.await())
           DependencyContainer.getInstance().apiService.sendPushToken(pushToken) //берем из контейнера зависимостей
            Result.success()
        } catch (e: Exception){
            e.printStackTrace()
            Result.retry()
        }

}