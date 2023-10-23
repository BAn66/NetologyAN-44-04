package ru.netologia.nmedia.dao

//Для ROOM

//import androidx.lifecycle.LiveData
//import androidx.room.Dao
//import androidx.room.Insert
//import androidx.room.Query
////import ru.netologia.nmedia.dto.Post
//import ru.netologia.nmedia.entity.PostEntity
//
//@Dao
//interface PostDao {
//    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
//    fun getAll(): LiveData<List<PostEntity>>
//
//    @Insert
//    fun insert(post: PostEntity)
//
//    @Query("UPDATE PostEntity SET content = :text WHERE id = :id")
//    fun changeContentById(id: Long, text: String)
//    fun save(post: PostEntity) =
//        if (post.id == 0L) insert(post) else changeContentById(post.id, post.content)
//
//    @Query(
//        """
//                UPDATE PostEntity SET
//                    likes = likes + CASE WHEN likedByMe THEN -1 ELSE 1 END,
//                    LikedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
//                WHERE id = :id;
//            """
//    )
//    fun likeById(id: Long)
//
//    @Query("DELETE FROM PostEntity WHERE id = :id")
//    fun removeById(id: Long)
//
////    @Query(
////                """
////                UPDATE PostEntity SET
////                    shares = shares + 1
////                WHERE id = :id;
////            """
////    )
////    fun shareById(id: Long)
//
//    @Query("SELECT * FROM PostEntity WHERE id = :id")
//    fun getPostById(id: Long): PostEntity
//}
//
//
//// Для SQLite
////interface PostDao {
////    fun getAll(): List<Post>
////    fun save(post: Post): Post
////    fun likeById(id: Long)
////    fun removeById(id: Long)
////    fun shareById(id: Long)
////    fun getPostById(id: Long): Post
//}