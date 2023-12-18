package ru.netologia.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.netologia.nmedia.auth.AppAuth
import ru.netologia.nmedia.db.AppDb
import ru.netologia.nmedia.dto.Token
import ru.netologia.nmedia.model.FeedModelState
import ru.netologia.nmedia.repository.PostRepository
import ru.netologia.nmedia.repository.PostRepositoryImpl

class SignInViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository =
        PostRepositoryImpl(AppDb.getInstance(context = application).postDao())

    fun sendRequest(login: String, password: String) {
        viewModelScope.launch {
            try {
                val token: Token = repository.requestToken(login, password)
                AppAuth.getInstance().setAuth(token.id, token.token)
            } catch (e: Exception) {
            }
        }
    }
}