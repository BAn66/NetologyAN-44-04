//package ru.netologia.nmedia.repository
//
////Для ROOM
//
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.map
//import ru.netologia.nmedia.dao.PostDao
//import ru.netologia.nmedia.dto.Post
//import ru.netologia.nmedia.entity.PostEntity
//
////хранение данных с помощью ROOM
//
//class PostRepositoryRoomImpl(
//    private val dao: PostDao
//)
//    : PostRepository {
//    override fun getAll(): LiveData<List<Post>> = dao.getAll().map {list ->
//        list.map{
//            it.toDto()}
//    }
//
//
//    override fun save(post: Post)  = dao.save(PostEntity.fromDto(post))
//
//    override fun likeById(id: Long)  = dao.likeById(id)
//
//    override fun removeById(id: Long) = dao.removeById(id)
//
//    override fun shareById(id: Long) = dao.shareById(id)
//
//    override fun getPostById(id: Long): Post = dao.getPostById(id).toDto()
//
//}
