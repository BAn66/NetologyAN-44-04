package ru.netologia.nmedia.dao

//Для ROOM

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.OnConflictStrategy
//import ru.netologia.nmedia.dto.Post
import ru.netologia.nmedia.entity.PostEntity

@Dao
interface PostDao {
    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    fun getAll(): LiveData<List<PostEntity>>

    @Query("SELECT COUNT(*) == 0 FROM PostEntity")
    suspend fun isEmpty(): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE) //это для сохранения и редактирования, replace заменяет если есть такой же айди кажется
    suspend fun insert(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE) //это для getall
    suspend fun insert(posts: List<PostEntity>)

    @Query("DELETE FROM PostEntity WHERE id = :id")
    suspend fun removeById(id: Long)

//    @Query("UPDATE PostEntity SET content = :text WHERE id = :id")
//    suspend fun changeContentById(id: Long, text: String)
//
//    suspend fun save(post: PostEntity) =
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
    @Query("SELECT * FROM PostEntity WHERE id = :id")
    fun getPostById(id: Long): PostEntity


//    @Query(
//                """
//                UPDATE PostEntity SET
//                    shares = shares + 1
//                WHERE id = :id;
//            """
//    )
//    fun shareById(id: Long)


}


// Для SQLite
//interface PostDao {
//    fun getAll(): List<Post>
//    fun save(post: Post): Post
//    fun likeById(id: Long)
//    fun removeById(id: Long)
//    fun shareById(id: Long)
//    fun getPostById(id: Long): Post
//}