package ru.netologia.nmedia.viewmodel

//import android.app.Application
//import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netologia.nmedia.auth.AppAuth
//import ru.netologia.nmedia.db.AppDb
//import ru.netologia.nmedia.di.DependencyContainer
import ru.netologia.nmedia.dto.Token
//import ru.netologia.nmedia.model.FeedModelState
import ru.netologia.nmedia.repository.PostRepository
//import ru.netologia.nmedia.repository.PostRepositoryImpl
import javax.inject.Inject

//class SignInViewModel(application: Application) : AndroidViewModel(application) { // До внедрения зависимости
@HiltViewModel
    class SignInViewModel @Inject constructor(
    private val repository: PostRepository,
    private val appAuth: AppAuth,
    ) : ViewModel() {
//    private val repository: PostRepository = // До внедрения зависимости
//        PostRepositoryImpl(dependencyContainer.(context = application).postDao(), dependencyContainer.apiService )

    fun sendRequest(login: String, password: String) {
        viewModelScope.launch {
            try {
                val token: Token = repository.requestToken(login, password)
                appAuth.setAuth(token.id, token.token)
            } catch (e: Exception) {
            }
        }
    }


}