package ru.netologia.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import ru.netologia.nmedia.dto.Post
import ru.netologia.nmedia.db.AppDb
import ru.netologia.nmedia.repository.PostRepository
import ru.netologia.nmedia.repository.PostRepositoryRoomImpl
import java.util.Calendar

//import ru.netologia.nmedia.repository.PostRepositorySQLiteImpl

private val empty = Post(
    id = 0,
    author = "",
    published = "",
    content = "",
    likedByMe = false,
    likes = 0,
//    isShare = false,
    shares = 0,
    views = 0,
    video = ""
)

//class PostViewModel (application: Application): ViewModel() {

//Для работы с хранением данных лучше использовать AndroidViewModel
class PostViewModel (application: Application): AndroidViewModel(application) {

//  Работа с рефами или с хардовой записью данных
//    private val repository: PostRepository = PostRepositoryInMemoryImpl(application)

    //Работа с хранением данных в файлах
//    private val repository: PostRepository = PostRepositoryFileImpl(application)


    //Работа с хранением данных в SQL
//    private val repository: PostRepository = PostRepositorySQLiteImpl(
//        AppDb.getInstance(application).postDao
//    )

    //Работа с хранением данных в ROOM
    private val repository: PostRepository = PostRepositoryRoomImpl(
        AppDb.getInstance(application).postDao
    )

    val data = repository.getAll()
    val edited = MutableLiveData(empty)
    fun likeById(id: Long) = repository.likeById(id)
    fun shareById(id: Long) = repository.shareById(id)
    fun removeById(id: Long) = repository.removeById(id)

//    fun getPostById(id: Long) = repository.getPostById(id)


//    fun save() { //функция сохранения в репозитории
//        edited.value?.let{
//            repository.save(it)
//        }
//
//    }

    fun changeContentAndSave(content: String) { //функция изменения и сохранения в репозитории
        edited.value?.let {
            val text = content.trim() //убираем пробелы вначале и конце
            if (it.content != text) {
                repository.save(it.copy(
                    content = text,
                    author = "Me",
                    published = Calendar.getInstance().time.toString()
                ))
            }
        }
        edited.value = empty
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun emptyNew(){
        edited.value = empty
    }


}


