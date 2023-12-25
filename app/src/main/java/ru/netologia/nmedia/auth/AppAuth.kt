package ru.netologia.nmedia.auth

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
//import ru.netologia.nmedia.api.PostsApi
import ru.netologia.nmedia.work.SendPushTokenWorker
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAuth @Inject constructor(context: Context) { //делаем синглтон с реализацией в компанион обжекте

    private val contextForWorker = context
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    private val _authState = MutableStateFlow<AuthState>(
        AuthState(
            prefs.getLong("id", 0L),
            prefs.getString("token", null)
        )
    )

    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    @Synchronized
    fun setAuth(id: Long, token: String) {
        _authState.value = AuthState(id, token)
        with(prefs.edit()) {//запись в префс айди пользователя и токена для авторизации
//            putLong(KEY_ID, id)
//            putString(KEY_TOKEN, token)
            putLong("id", id)
            putString("token", token)
            commit()
        }
        sendPushToken()
    }


    @Synchronized
    fun removeAuth() {
        _authState.value = AuthState(0, null) //запись в префс нулевых значений авторизации
        with(prefs.edit()) {
            clear()
            commit()
        }
        sendPushToken()
    }


    fun sendPushToken(token: String? = null) { //PUSHes // запускается при каком либо изменении авторизации (добавил в методах выше)
        //отправка токена отсюда
//        CoroutineScope(Dispatchers.Default).launch {
//            try {
//                val pushToken = PushToken(token ?: FirebaseMessaging.getInstance().token.await())
//                DependencyContainer.getInstance().apiService.sendPushToken(pushToken) //берем из контейнера зависимостей
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }

//        c помощью воркера
        val request = OneTimeWorkRequestBuilder<SendPushTokenWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setInputData(
                Data.Builder()
                    .putString(SendPushTokenWorker.TOKEN_KEY, token)
                    .build()
            )
            .build()


        WorkManager.getInstance(contextForWorker)
            .enqueueUniqueWork(
                SendPushTokenWorker.NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
    }

    // Не нужен так как есть внедрение зависимостей
//    companion object {
//        private const val KEY_ID = "id"
//        private const val KEY_TOKEN = "token"
//
//        @SuppressLint("StaticFieldLeak")
//        @Volatile
//        private var instance: AppAuth? = null
//
//        fun getInstance() = synchronized(this) {
//            instance
//                ?: throw IllegalStateException("getInstance should be called only after initAuth")
//        }
//
//        fun initAuth(context: Context) = instance
//            ?: synchronized(this) {//классический вариант даблчеклокинг когда синглтон учитывает многопоточность
//                instance ?: AppAuth(context).also { instance = it }
//            }
//    }

    data class AuthState(val id: Long = 0L, val token: String? = null)
}
