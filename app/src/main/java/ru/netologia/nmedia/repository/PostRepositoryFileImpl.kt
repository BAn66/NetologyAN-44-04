package ru.netologia.nmedia.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netologia.nmedia.dto.Post
import java.util.Calendar


class PostRepositoryFileImpl(
    private val context: Context
) : PostRepository {
    private val gson = Gson()
    private var posts = emptyList<Post>()
    private val postsFileName= "post.json"
    private val type = TypeToken.getParameterized(List::class.java, Post::class.java).type

    private val data = MutableLiveData(posts)
        init {
        val postFile = context.filesDir.resolve(postsFileName)

        posts = if (postFile.exists()) {
            postFile.reader().buffered().use {
                gson.fromJson(it, type)
            }
        } else {
            emptyList()
        }

        data.value = posts
    }

    override fun getAll(): LiveData<List<Post>> = data

    override fun getPostById(id: Long): Post = posts.filter { it.id == id }[0].copy()

    override fun likeById(id: Long) {
        posts = posts.map {
            if (it.id != id) it else it.copy(
                likedByMe = !it.likedByMe,
                likes = if (it.likedByMe) it.likes - 1 else it.likes + 1
            )
        }
        data.value = posts
        sync() //После каждого изменения синхронизируем данные с файлом
    }

    override fun shareById(id: Long) {
        posts = posts.map {
            if (it.id != id) it else it.copy(
//                isShare = true,
                shares = it.shares + 1
            )
        }
        data.value = posts
        sync()
    }

    override fun removeById(id: Long) {
        posts = posts.filter { it.id != id }
        data.value = posts
        sync()
    }

    override fun save(post: Post) {
        posts = if (post.id == 0L) {
            listOf(
                post.copy(
                    id = (posts.map { it.id }.toMutableList().maxOrNull() //поиск самого большого айди в списке постов
                        ?: 0) + 1,//Берем макс значения id в списке постов +1
                    author = "Me",
                    likedByMe = false,
                    published = Calendar.getInstance().time.toString()
                )
            ) + posts
        } else {
            posts.map {
                if (it.id != post.id) it else it.copy(
                    content = post.content
                )
            }
        }
        data.value = posts

        sync()
        //return
    }

    private fun sync(){
        context.filesDir.resolve(postsFileName).writer().buffered().use(){
            it.write(gson.toJson(posts))
        }
    }


}
