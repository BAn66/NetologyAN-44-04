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
import kotlin.concurrent.thread

private val empty = Post(
    id = 0L,
    author = "",
    published = 0L,
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
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit> = _postCreated

    var idCount = 0L

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
            }.let(_data::postValue) //или так для красоты, это называется референс

//            _data.postValue( //можно так для красоты
//            try {
//                //Данные успешно получены
//                val posts = repository.getAll()
//                FeedModelState(posts = posts, empty = posts.isEmpty())
//            } catch (e: Exception) {
//                //Получена ошибка
//                FeedModelState(error = true)
//            }) //или так, это называется референс
        }
    }

    fun likeById(id: Long) = repository.likeById(id)
    fun shareById(id: Long) = repository.shareById(id)
    fun removeById(id: Long) = repository.removeById(id)

    fun changeContentAndSave(content: String, resultId: Long) {
        thread {  //функция изменения и сохранения в репозитории
            edited.value?.let { editedPost ->
                val text: String = content.trim()//убираем пробелы вначале и конце
//__________________________________________________________________________________
//                val posts = repository.getAll().toMutableList()
//                var postAfterEdited = empty
//                Log.d("MY LOOOOOOG ", "${resultId}")
//                if (resultId == 0L) {
//                    if (editedPost.content != text) {
//                        postAfterEdited = editedPost.copy(
//                            id = ((posts.map { it.id }.toMutableList()
//                                .maxOrNull() //поиск самого большого айди в списке постов
//                                ?: 0) + 1),
//                            content = text,
//                            author = "Me post",
//                            published = System.currentTimeMillis()
//                        )
//                    }
//                } else {
//                    postAfterEdited = editedPost.copy(
//                        id = resultId,
//                        content = text,
//                        author = "Me post",
//                        published = System.currentTimeMillis()
//                    )
//                }
//                Log.d("MY LOOOOOOG ", postAfterEdited.toString())
//
//                repository.save(postAfterEdited)
//                __________________________________________________________________________________

//                if (editedPost.id == 0L) {
//                    repository.save(
//                        editedPost.copy(
//                            id = (posts.map { it.id }.toMutableList()
//                                .maxOrNull() //поиск самого большого айди в списке постов
//                                ?: 0) + 1,//Берем макс значения id в списке постов +1
//                            content = text,
//                            author = "Me post",
//                            published = System.currentTimeMillis()
//                        )
//                    )
//                }

//                __________________________________________________________________________________
//                if (editedPost.content != text) {
//                    repository.save(
//                        editedPost.copy(//
//                            content = text,
//                            author = "Me post",
//                            published = System.currentTimeMillis()
//                        )
//                    )
//                }
//                __________________________________________________________________________________
                if (resultId == 0L) {
                    idCount = idCount +1
                    repository.save(
                        editedPost.copy(
//                            id = idCount,
                            content = text,
                            author = "Me post",
                            published = System.currentTimeMillis()
                        )
                    )
                }

            }
//                val post = repository.save(postAfterEdit)

            load() // можно так или как ниже обработать ответ от сервера

//                val value = _data.value
//
//                val updatePosts = value?.posts?.map {
//                    if (it.id == editedPost.id) {
//                        post
//                    } else {
//                        it
//                    }
//                }.orEmpty()
//
//                val result = if (value?.posts == updatePosts) {
//                    listOf(post) + updatePosts
//                } else {
//                    updatePosts
//                }
//
//                _data.postValue(
//                    value?.copy(posts = result)
//                )


            _postCreated.postValue(Unit) //работа с SingleLiveEvent
            edited.postValue(empty)
        }
    }

    fun edit(post: Post) {
//        edited.postValue(post)
        edited.value = post
    }

    fun emptyNew() {
//        edited.postValue(empty)
        edited.value = empty
    }


}


