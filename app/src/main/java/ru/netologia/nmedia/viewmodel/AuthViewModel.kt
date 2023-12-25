package ru.netologia.nmedia.viewmodel

//import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
//import androidx.lifecycle.asLiveData
import ru.netologia.nmedia.auth.AppAuth
//import ru.netologia.nmedia.di.DependencyContainer
//import ru.netologia.nmedia.repository.PostRepository

class AuthViewModel(

    private val appAuth: AppAuth
): ViewModel() {
    val data
//    : LiveData<AppAuth.AuthState>
    =
//        AppAuth.getInstance()// До внедрения зависимостей
    appAuth.authState
//        .asLiveData()

    val authenticated: Boolean
        get() = appAuth.authState.value.id != 0L
}