package ru.netologia.nmedia.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netologia.nmedia.auth.AppAuth
import ru.netologia.nmedia.dto.Token
import ru.netologia.nmedia.repository.PostRepository
import javax.inject.Inject

@HiltViewModel
    class SignInViewModel @Inject constructor(
    private val repository: PostRepository,
    private val appAuth: AppAuth,
    ) : ViewModel() {
    fun sendRequest(login: String, password: String) {
        viewModelScope.launch {
            try {
                val token: Token = repository.requestToken(login, password)
                appAuth.setAuth(token.id, token.token)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}