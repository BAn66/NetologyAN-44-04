package ru.netologia.nmedia.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netologia.nmedia.dto.Post
import ru.netologia.nmedia.model.FeedModelState
import ru.netologia.nmedia.repository.PostRepository
import ru.netologia.nmedia.repository.PostRepositoryImpl
import ru.netologia.nmedia.util.SingleLiveEvent
import java.io.IOException
import kotlin.concurrent.thread

private val empty = Post(
    id = 0L,
    author = "",
    published = 0L,
    content = "",
    likedByMe = false,
    likes = 0L,
    shares = 0L,
    views = 0L,
    video = ""
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
//    //Работа с хранением данных в ROOM
//    private val repository: PostRepository = PostRepositoryRoomImpl(
//        AppDb.getInstance(application).postDao
//    )

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
        thread {
            _data.postValue(FeedModelState(loading = true)) //Начинаем загрузку
            try {
                //Данные успешно получены
                val posts = repository.getAll()
                FeedModelState(posts = posts, empty = posts.isEmpty())
            } catch (e: Exception) {
                //Получена ошибка
                FeedModelState(error = true)
            }.also(_data::postValue) //или так для красоты, это называется референс

//            _data.postValue( //можно так для красоты
//            try {
//                //Данные успешно получены
//                val posts = repository.getAll()
//                FeedModelState(posts = posts, empty = posts.isEmpty())
//            } catch (e: Exception) {
//                //Получена ошибка
//                FeedModelState(error = true)
//            })
        }
    }

    fun likeById(id: Long) {
        thread {
        repository.likeById(id, repository.getPostById(id).likedByMe)
            _data.postValue(
                _data.value?.copy(posts = repository.getAll())
            )
        }
    }

    fun shareById(id: Long) = repository.shareById(id)
    fun removeById(id: Long) {
        thread {
            // Оптимистичная модель
            val old = _data.value?.posts.orEmpty()
            _data.postValue(
                _data.value?.copy(posts = _data.value?.posts.orEmpty()
                    .filter { it.id != id }
                )
            )
            try {
                repository.removeById(id)
            } catch (e: IOException) {
                _data.postValue(_data.value?.copy(posts = old))
            }
        }
    }

    fun changeContentAndSave(content: String) {
        //функция изменения и сохранения в репозитории
        edited.value?.let { editedPost ->
            thread {
                val text: String = content.trim()//убираем пробелы вначале и конце

                if (editedPost.content != text) {
                    val post = repository.save(
                            editedPost.copy(
                                content = text,
                                author = "me",
                                published = System.currentTimeMillis()
                            )
                        )
                    Log.d("МОЙ ЛОГ", post.toString())

                val value = _data.value

                val updatePosts = value?.posts?.map {
                    if (it.id == editedPost.id) {
                        post
                    } else {
                        it
                    }
                }.orEmpty()

                val result = if (value?.posts == updatePosts) {
                    listOf(post) + updatePosts
                } else {
                    updatePosts
                }

                _data.postValue(
                    value?.copy(posts = result)
                )
                }

                //работа с SingleLiveEvent
                _postCreated.postValue(Unit)
            }
            edited.postValue(empty)
        }
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun emptyNew() {
        edited.value = empty
    }

}
