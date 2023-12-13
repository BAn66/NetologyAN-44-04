package ru.netologia.nmedia.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
//import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netologia.nmedia.auth.AppAuth
import ru.netologia.nmedia.db.AppDb
import ru.netologia.nmedia.dto.Post
import ru.netologia.nmedia.model.FeedModel
import ru.netologia.nmedia.model.FeedModelState
import ru.netologia.nmedia.model.PhotoModel
import ru.netologia.nmedia.repository.PostRepository
import ru.netologia.nmedia.repository.PostRepositoryImpl
import ru.netologia.nmedia.util.SingleLiveEvent
import java.io.File


private val empty = Post(
    id = 0L,
    author = "",
    authorId = 0L,
    authorAvatar = "",
    published = 0L,
    content = "",
    likedByMe = false,
    likes = 0,
    attachment = null
//    shares = 0L,
//    views = 0L,
//    video = ""
)

//class PostViewModel(application: Application, myRepository: PostRepository) :
class PostViewModel(application: Application) : AndroidViewModel(application) {
    // Работа с сетевыми запросами
    private val repository: PostRepository =
        PostRepositoryImpl(AppDb.getInstance(context = application).postDao())
    var haveNew: Boolean =
        true //TODO здесь должна быть функция которая говорит есть ли новые не загруженные посты


    //    val data: LiveData<FeedModel> = repository.data.map(::FeedModel) //Все посты в внутри фиидмодели //Без Flow

    @OptIn(ExperimentalCoroutinesApi::class)
    val data: LiveData<FeedModel> = AppAuth.getInstance()//Добавляем флоу для Auth
        .authState
        .flatMapLatest {auth ->
            repository.data
                .map {posts ->
                    FeedModel(
                        posts.map{it.copy(ownedByMe = auth.id == it.authorId)},
                        posts.isEmpty()
                    )
                }
                .catch {
                    errorMessage = repository.getErrMess()
                }
        }
        .asLiveData(Dispatchers.Default)

//        repository.data //только для флоу c постами
//        .map(::FeedModel)
//        .catch {
//            errorMessage = repository.getErrMess()
////                _dataState.value = FeedModelState(error = true)
//        }
//        .asLiveData(Dispatchers.Default) //запускаем не на главном потоке

    private val _photo = MutableLiveData<PhotoModel?>(null)  //Для картинок
    val photo: LiveData<PhotoModel?>
        get() = _photo

    val newerCount = data.switchMap {
        repository.getNewer(it.posts.firstOrNull()?.id ?: 0L)
            .asLiveData(Dispatchers.Default)

    }


    private val _dataState = MutableLiveData(FeedModelState()) //Состояние
    val dataState: LiveData<FeedModelState>
        get() = _dataState
    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    var errorMessage: Pair<Int, String> = Pair(0, "")

    init {
        loadPosts()
    }

    fun setPhoto(uri: Uri, file: File) {
        _photo.value = PhotoModel(uri, file)
    }

    fun loadPosts() = viewModelScope.launch { //Загружаем посты c помщью коротюнов и вьюмоделскоуп
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.getAll()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            errorMessage = repository.getErrMess()
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun refreshPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(refreshing = true)
            haveNew = repository.haveNewer()
            repository.getAll()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            errorMessage = repository.getErrMess()
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun showNewPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(refreshing = true)
            haveNew = repository.haveNewer()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            errorMessage = repository.getErrMess()
            _dataState.value = FeedModelState(error = true)
        }
    }


    fun changeContentAndSave(content: String) {
        val text: String = content.trim()
        //функция изменения и сохранения в репозитории
        edited.value?.let {
            val postCopy = it.copy(
//                authorId = 555,
                author = "me",
                content = text,
                published = System.currentTimeMillis(),
//                ownedByMe = true
            )
            viewModelScope.launch {
                try {
                    val photoModel = _photo.value
                    if (photoModel == null && it.content != text) {
                        repository.save(postCopy)

                    } else if (photoModel != null && it.content != text) {
                        repository.saveWithAttachment(postCopy, photoModel)
                    }
                    _dataState.value = FeedModelState()
                    _postCreated.value = Unit
                } catch (e: Exception) {
                    errorMessage = repository.getErrMess()
                    _dataState.value = FeedModelState(error = true)
                }
            }
        }
        emptyNew()
    }

    fun edit(post: Post) {
        edited.value = post
    }


    fun emptyNew() {
        edited.value = empty
    }

    //    fun shareById(id: Long) = repository.shareById(id)

    fun likeById(id: Long, likedByMe: Boolean) {
        viewModelScope.launch {
            try {
                repository.likeById(id, likedByMe)
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                errorMessage = repository.getErrMess()
                _dataState.value = FeedModelState(error = true)
            }
        }

//                repository.likeByIdAsync(id, likedByMe, object : PostRepository.SaveCallback {
//                    override fun onSuccess(result: Unit) {
////                val updatePosts = _data.value?.posts?.map {
////                    if (it.id == id) {
////                        result
////                    } else {
////                        it
////                    }
////                }.orEmpty()
////                val resultList = if (_data.value?.posts == updatePosts) {
////                    listOf(result) + updatePosts
////                } else {
////                    updatePosts
////                }
////                _data.postValue(
////                    _data.value?.copy(posts = resultList )
////                )
//                        loadPosts()
//                    }
//
//                    fun onError(e: Exception) {
////                _data.postValue(FeedModelState(error = true))
//                        _dataState.value = FeedModelState(error = true)
//                        errorMessage = repository.getErrMess()
//                    }
//                }
//                )
    }

    fun removeById(id: Long) {
        viewModelScope.launch {
            try {
                repository.removeById(id)
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                errorMessage = repository.getErrMess()
                _dataState.value = FeedModelState(error = true)
            }
        }

//        repository.removeByIdAsync(id, object : PostRepository.SaveCallback {
//            override fun onSuccess(result: Unit) {
//                loadPosts()
//            }
//
//            override fun onError(e: Exception) {
//                _dataState.value = FeedModelState(error = true)
//                errorMessage = repository.getErrMess()
//            }
//        })
//        // Оптимистичная модель
////        val old = _data.value?.posts.orEmpty()
////        val newResult = _data.value?.copy(posts = _data.value?.posts.orEmpty()
////            .filter { it.id != id })
////        repository.removeByIdAsync(id, object : PostRepository.GetAllCallback<Post> {
////            override fun onSuccess(result: Post) {
////                _data.postValue(newResult)
////            }
////            override fun onError(e: Exception) {
////                _data.postValue(_data.value?.copy(posts = old))
////            }
////
////        })
    }

    fun clearPhoto() {
        _photo.value = null
    }
}


// Old version
//class PostViewModel(application: Application) : AndroidViewModel(application) {
//
//
//    // Работа с сетевыми запросами
//    private val repository: PostRepository = PostRepositoryImpl() // в продакшене так лучше не делать, а переносить обявление репозитория в конструктор.
//    private val _data = MutableLiveData(FeedModelState())
//
//    val data: LiveData<FeedModelState> = _data
//    private val _postCreated = SingleLiveEvent<Unit>()
//    val postCreated: LiveData<Unit> = _postCreated
//    val edited = MutableLiveData(empty)
//
//    var errorMessage : Pair<Int, String> = Pair(0, "")
//
//    init {
//        load()
//    }
//
//
//    fun load() {
////        _data.postValue(FeedModelState(loading = true)) //Начинаем загрузку в okhttp на фоновом потоке поэтому postValue
//        _data.value =
//            FeedModelState(loading = true) //Начинаем загрузку через value потому что retrofit работает на главном потоке
//        //В Асинхронный вариант thread не нужен, потому что асинхрон реализован уже в enqueue библиотеки okhttp
//
//        repository.getAllAsync(object : PostRepository.GetAllCallback<List<Post>> {
//            override fun onSuccess(result: List<Post>) {
////                _data.postValue(FeedModelState(posts = result, empty = result.isEmpty())) // okhttp
//                _data.value = FeedModelState(posts = result, empty = result.isEmpty()) //retrofit
//            }
//
//            override fun onError(e: Exception) {
////                _data.postValue(FeedModelState(error = true)) // okhttp
//
//                _data.value = FeedModelState(error = true) //retrofit
//                errorMessage = repository.getErrMess()
//            }
//        })
//    }
//
//
//    fun likeById(id: Long, likedByMe: Boolean) {
//        repository.likeByIdAsync(id, likedByMe, object : PostRepository.SaveCallback {
//            override fun onSuccess(result: Unit) {
////                val updatePosts = _data.value?.posts?.map {
////                    if (it.id == id) {
////                        result
////                    } else {
////                        it
////                    }
////                }.orEmpty()
////                val resultList = if (_data.value?.posts == updatePosts) {
////                    listOf(result) + updatePosts
////                } else {
////                    updatePosts
////                }
////                _data.postValue(
////                    _data.value?.copy(posts = resultList )
////                )
//                load()
//            }
//
//            override fun onError(e: Exception) {
////                _data.postValue(FeedModelState(error = true))
//                _data.value = FeedModelState(error = true)
//                errorMessage = repository.getErrMess()
//            }
//        }
//        )
//    }
//
//    //    fun shareById(id: Long) = repository.shareById(id)
//
//    fun removeById(id: Long) {
//        repository.removeByIdAsync(id, object : PostRepository.SaveCallback {
//            override fun onSuccess(result: Unit) {
//                load()
//            }
//
//            override fun onError(e: Exception) {
//                _data.value = FeedModelState(error = true)
//                errorMessage = repository.getErrMess()
//            }
//        })
//        // Оптимистичная модель
////        val old = _data.value?.posts.orEmpty()
////        val newResult = _data.value?.copy(posts = _data.value?.posts.orEmpty()
////            .filter { it.id != id })
////        repository.removeByIdAsync(id, object : PostRepository.GetAllCallback<Post> {
////            override fun onSuccess(result: Post) {
////                _data.postValue(newResult)
////            }
////            override fun onError(e: Exception) {
////                _data.postValue(_data.value?.copy(posts = old))
////            }
////
////        })
//    }
//
//    fun changeContentAndSave(content: String) {
//        //функция изменения и сохранения в репозитории
//        edited.value?.let { editedPost ->
//            val text: String = content.trim()
//            if (editedPost.content != text) {
//                repository.saveAsync(
//                    editedPost.copy(
//                        content = text,
//                        author = "me",
//                        published = System.currentTimeMillis()
//                    ),
//                    object : PostRepository.GetAllCallback<Post> {
//                        override fun onSuccess(result: Post) {
//                            val value = _data.value
//
//                            val updatePosts = value?.posts?.map {
//                                if (it.id == editedPost.id) {
//                                    result
//                                } else {
//                                    it
//                                }
//                            }.orEmpty()
//
//                            val resultList: List<Post> = if (value?.posts == updatePosts) {
//                                listOf(result) + updatePosts
//                            } else {
//                                updatePosts
//                            }
//
//                            _data.value = value?.copy(posts = resultList)
//
//                            //работа с SingleLiveEvent
//                            _postCreated.value = Unit
//                        }
//
//                        override fun onError(e: Exception) {
//                            _data.value = FeedModelState(error = true)
//                            errorMessage = repository.getErrMess()
//
//                        }
//                    }
//                )
//            }
//            edited.value = empty
//        }
//    }
//
//    fun edit(post: Post) {
//        edited.value = post
//    }
//
//    fun emptyNew() {
//        edited.value = empty
//    }
//
//}

