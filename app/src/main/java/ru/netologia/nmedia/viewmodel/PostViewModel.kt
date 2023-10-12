package ru.netologia.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netologia.nmedia.dto.Post
import ru.netologia.nmedia.db.AppDb
import ru.netologia.nmedia.model.FeedModelState
import ru.netologia.nmedia.repository.PostRepository
import ru.netologia.nmedia.repository.PostRepositoryImpl
import ru.netologia.nmedia.repository.PostRepositoryRoomImpl
import java.util.Calendar
import kotlin.concurrent.thread

private val empty = Post(
    id = 0,
    author = "",
    published = 0,
    content = "",
    likedByMe = false,
    likes = 0,
    shares = 0,
    views = 0,
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
    val edited = MutableLiveData(empty)

    init {
        load()
    }

    fun load() {
        thread {
            _data.postValue(FeedModelState(loading = true)) //Начинаем загрузку

//            _data.postValue( //можно так для красоты
            try {
                //Данные успешно получены
                val posts = repository.getAll()
                FeedModelState(posts = posts, empty = posts.isEmpty())
            } catch (e: Exception) {
                //Получена ошибка
                FeedModelState(error = true)
            }.let { _data::postValue } //или так, это называется референс
//            )

        }
    }

    fun likeById(id: Long) = repository.likeById(id)
    fun shareById(id: Long) = repository.shareById(id)
    fun removeById(id: Long) = repository.removeById(id)

    fun changeContentAndSave(content: String) {
        thread {  //функция изменения и сохранения в репозитории
            edited.value?.let {
                val text = content.trim() //убираем пробелы вначале и конце
                if (it.content != text) {
                    repository.save(
                        it.copy(
                            content = text,
                            author = "Me",
                            published = System.currentTimeMillis()
                        )
                    )
                    load()
                }

            }
            edited.postValue(empty)
        }
    }

    fun edit(post: Post) {
        edited.postValue(post)
    }

    fun emptyNew() {
        edited.postValue(empty)
    }


}


