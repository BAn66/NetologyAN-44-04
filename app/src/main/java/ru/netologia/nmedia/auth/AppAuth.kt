package ru.netologia.nmedia.auth

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ru.netologia.nmedia.api.PostsApi
import ru.netologia.nmedia.dto.PushToken
import java.lang.IllegalStateException

class AppAuth private constructor(context: Context) { //делаем синглтон с реализацией в компанион обжекте

    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    private val _authState = MutableStateFlow<AuthState>(
       AuthState(
           prefs.getLong(KEY_ID, 0L),
           prefs.getString(KEY_TOKEN, null)
       )
    )

    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    @Synchronized
    fun setAuth(id: Long, token: String){
        _authState.value = AuthState(id, token)
        with(prefs.edit()){//запись в префс айди пользователя и токена для авторизации
            putLong(KEY_ID, id)
            putString(KEY_TOKEN, token)
            commit()
        }
        sendPushToken()
    }


    @Synchronized
    fun removeAuth(){
        _authState.value = AuthState(0, null) //запись в префс нулевых значений авторизации
        with(prefs.edit()){
            clear()
            commit()
        }
        sendPushToken()
    }


    fun sendPushToken(token: String? = null ){ //PUSHes // запускается при каком либо изменении авторизации (добавил в методах выше)
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val pushToken = PushToken(token ?: FirebaseMessaging.getInstance().token.await())
                PostsApi.retrofitService.sendPushToken(pushToken)
            } catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

    companion object {
        private const val KEY_ID = "id"
        private const val KEY_TOKEN = "token"

        @Volatile
        private var instance: AppAuth? = null

        fun getInstance() = synchronized(this) {
            instance
                ?: throw IllegalStateException("getInstance should be called only after initAuth")
        }

        fun initAuth(context: Context) = instance ?: synchronized(this) {//классический вариант даблчеклокинг когда синглтон учитывает многопоточность
                instance ?: AppAuth(context).also { instance = it }
            }
    }

    data class AuthState(val id: Long = 0L, val token: String? = null)
}
