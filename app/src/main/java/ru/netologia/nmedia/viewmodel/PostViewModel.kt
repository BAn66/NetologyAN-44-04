package ru.netologia.nmedia.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netologia.nmedia.auth.AppAuth
import ru.netologia.nmedia.dto.Post
import ru.netologia.nmedia.model.FeedModelState
import ru.netologia.nmedia.model.PhotoModel
import ru.netologia.nmedia.repository.PostRepository
import ru.netologia.nmedia.util.SingleLiveEvent
import java.io.File
import javax.inject.Inject

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
)
@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    appAuth: AppAuth
):ViewModel(){

    var haveNew: Boolean = false //маркер для всплывашки "Обновить"

    private var maxId = MutableStateFlow(0L) //Для получения максимального id из текущего списка

    @OptIn(ExperimentalCoroutinesApi::class)
    val data: Flow<PagingData<Post>> = appAuth
        .authStateFlow
        .flatMapLatest {auth ->
            repository.data
                .map { pagingData ->
                    pagingData.map { post ->
                        maxId.value = maxOf(post.id, maxId.value)// сравнение текущего макс.ид и ид в паггинге
                        post.copy(ownedByMe = auth.id == post.authorId)
                    }
                }
                .catch {
                    errorMessage = repository.getErrMess()
                }
        }
        .flowOn(Dispatchers.Default)

    @OptIn(ExperimentalCoroutinesApi::class)
    val newerCount = maxId.flatMapLatest { id ->
        repository.getNewer(id)
    }.asLiveData(Dispatchers.Default)

    private val _photo = MutableLiveData<PhotoModel?>(null)  //Для картинок
    val photo: LiveData<PhotoModel?>
        get() = _photo

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

    fun loadPosts() = viewModelScope.launch { //Загружаем посты c помщью коротюнов и вьюмоделскоуп
        try {
            _dataState.value = FeedModelState(loading = true)
            haveNew = repository.switchNewOnShowed()
            repository.getAll()
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
                author = "me",
                content = text,
                published = System.currentTimeMillis(),
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
    }

    fun setPhoto(uri: Uri, file: File) {
        _photo.value = PhotoModel(uri, file)
    }

    fun clearPhoto() {
        _photo.value = null
    }
}
