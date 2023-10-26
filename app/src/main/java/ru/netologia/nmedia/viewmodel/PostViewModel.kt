package ru.netologia.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netologia.nmedia.dto.Post
import ru.netologia.nmedia.model.FeedModelState
import ru.netologia.nmedia.repository.PostRepository
import ru.netologia.nmedia.repository.PostRepositoryImpl
import ru.netologia.nmedia.util.SingleLiveEvent


private val empty = Post(
    id = 0L,
    author = "",
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

class PostViewModel(application: Application) : AndroidViewModel(application) {


    // Работа с сетевыми запросами
    private val repository: PostRepository = PostRepositoryImpl()
    private val _data = MutableLiveData(FeedModelState())
    val data: LiveData<FeedModelState> = _data
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit> = _postCreated
    val edited = MutableLiveData(empty)

    init {
        load()
    }

    fun load() {
//        _data.postValue(FeedModelState(loading = true)) //Начинаем загрузку в okhttp на фоновом потоке поэтому postValue
        _data.value =
            FeedModelState(loading = true) //Начинаем загрузку через value потому что retrofit работает на главном потоке
        //В Асинхронный вариант thread не нужен, потому что асинхрон реализован уже в enqueue библиотеки okhttp
        repository.getAllAsync(object : PostRepository.GetAllCallback<List<Post>> {
            override fun onSuccess(result: List<Post>) {
//                _data.postValue(FeedModelState(posts = result, empty = result.isEmpty())) // okhttp
                _data.value = FeedModelState(posts = result, empty = result.isEmpty()) //retrofit
            }

            override fun onError(e: Exception) {
//                _data.postValue(FeedModelState(error = true)) // okhttp
                _data.value = FeedModelState(error = true) //retrofit
            }
        })
    }


    fun likeById(id: Long, likedByMe: Boolean) {
        repository.likeByIdAsync(id, likedByMe, object : PostRepository.GetAllCallback<Post> {
            override fun onSuccess(result: Post) {
                val updatePosts = _data.value?.posts?.map {
                    if (it.id == id) {
                        result
                    } else {
                        it
                    }
                }.orEmpty()
                val resultList = if (_data.value?.posts == updatePosts) {
                    listOf(result) + updatePosts
                } else {
                    updatePosts
                }
                _data.postValue(
                    _data.value?.copy(posts = resultList)
                )
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModelState(error = true))
            }
        }
        )
    }

    //    fun shareById(id: Long) = repository.shareById(id)
    fun removeById(id: Long) {
        // Оптимистичная модель
        val old = _data.value?.posts.orEmpty()
        val newResult = _data.value?.copy(posts = _data.value?.posts.orEmpty()
            .filter { it.id != id })
        repository.removeByIdAsync(id, object : PostRepository.GetAllCallback<Post> {
            override fun onSuccess(result: Post) {
                _data.postValue(newResult)
            }
            override fun onError(e: Exception) {
                _data.postValue(_data.value?.copy(posts = old))
            }

        })
    }

    fun changeContentAndSave(content: String) {
        //функция изменения и сохранения в репозитории
        edited.value?.let { editedPost ->
            val text: String = content.trim()
            if (editedPost.content != text) {
                repository.saveAsync(
                    editedPost.copy(
                        content = text,
                        author = "me",
                        published = System.currentTimeMillis()
                    ),
                    object : PostRepository.GetAllCallback<Post> {
                        override fun onSuccess(result: Post) {
                            val value = _data.value

                            val updatePosts = value?.posts?.map {
                                if (it.id == editedPost.id) {
                                    result
                                } else {
                                    it
                                }
                            }.orEmpty()

                            val resultList : List<Post> = if (value?.posts == updatePosts) {
                                listOf(result) + updatePosts
                            } else {
                                updatePosts
                            }

                            _data.value = value?.copy(posts = resultList)

                            //работа с SingleLiveEvent
                            _postCreated.value = Unit
                        }

                        override fun onError(e: Exception) {
                            _data.value = FeedModelState(error = true)
                        }
                    }
                )
            }
            edited.value = empty
        }
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun emptyNew() {
        edited.value = empty
    }

}
