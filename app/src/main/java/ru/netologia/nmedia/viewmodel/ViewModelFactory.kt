//При использование HILT не нужен
// package ru.netologia.nmedia.viewmodel
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.ViewModelProvider
//import ru.netologia.nmedia.auth.AppAuth
//import ru.netologia.nmedia.repository.PostRepository
//
//
//class ViewModelFactory(
//    private val repository: PostRepository,
//    private val appAuth: AppAuth
//): ViewModelProvider.Factory {
//
//    //Для подавление появления ошибки-исключения
//
//    @Suppress("UNCHECKED_CAST")
//
//    override fun <T : ViewModel> create(modelClass: Class<T>): T =
//        when{
//            modelClass.isAssignableFrom(PostViewModel::class.java)->{
//                PostViewModel(repository, appAuth) as T
//            }
//            modelClass.isAssignableFrom(AuthViewModel::class.java)->{
//                AuthViewModel(appAuth) as T
//            }
//            modelClass.isAssignableFrom(SignInViewModel::class.java)->{
//                SignInViewModel(repository, appAuth) as T
//            }
//            else -> error("Unknown class :$modelClass which we don't now how create")
//        }
//}