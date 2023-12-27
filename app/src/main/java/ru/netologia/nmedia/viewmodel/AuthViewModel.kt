package ru.netologia.nmedia.viewmodel

//import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
//import androidx.lifecycle.asLiveData
import ru.netologia.nmedia.auth.AppAuth
import javax.inject.Inject

//import ru.netologia.nmedia.di.DependencyContainer
//import ru.netologia.nmedia.repository.PostRepository

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val appAuth: AppAuth
): ViewModel() {
    val data
//    : LiveData<AppAuth.AuthState>
    =
//        AppAuth.getInstance()// До внедрения зависимостей
    appAuth.authStateFlow
//        .asLiveData()

    val authenticated: Boolean
        get() = appAuth.authStateFlow.value.id != 0L
}