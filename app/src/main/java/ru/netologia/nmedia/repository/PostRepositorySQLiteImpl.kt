package ru.netologia.nmedia.repository

//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import ru.netologia.nmedia.dao.PostDao
//import ru.netologia.nmedia.dto.Post

//хранение данных с помощью SQL
//class PostRepositorySQLiteImpl(
//    private val dao: PostDao
//) : PostRepository {
//    private var posts = emptyList<Post>()
//    private var data = MutableLiveData(posts)
//
//    init {
//        posts = dao.getAll()
//        data.value = posts
//    }
//
//    //Основные методы
//    override fun getAll(): LiveData<List<Post>> = data
//
//    override fun save(post: Post) {
//        val id = post.id
//        val saved = dao.save(post)
//        posts = if (id == 0L) {
//            listOf(saved) + posts
//        } else {
//            posts.map {
//                if (it.id != id) it else saved
//            }
//        }
//        data.value = posts
//    }
//
//    override fun likeById(id: Long) {
//        dao.likeById(id)
//        posts = posts.map {
//            if (it.id != id) it else it.copy(
//                likedByMe = !it.likedByMe,
//                likes = if (it.likedByMe) it.likes - 1 else it.likes + 1
//            )
//        }
//        data.value = posts
//    }
//
//    override fun removeById(id: Long) {
//        dao.removeById(id)
//        posts = posts.filter { it.id != id }
//        data.value = posts
//    }
//
//    override fun shareById(id: Long) {
//        dao.shareById(id)
//        posts = posts.map {
//            if (it.id != id) it else it.copy(
//                isShare = true,
//                shares = it.shares + 1
//            )
//        }
//        data.value = posts
//    }
//
//    override fun getPostById(id: Long): Post {
////        dao.getPostById(id)
//        return posts.filter { it.id == id }[0].copy()
//    }
//}