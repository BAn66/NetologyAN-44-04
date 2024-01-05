package ru.netologia.nmedia.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.OnConflictStrategy
import kotlinx.coroutines.flow.Flow
import ru.netologia.nmedia.entity.PostEntity

@Dao
interface PostDao {
    @Query("SELECT * FROM PostEntity WHERE showed = 1 ORDER BY id DESC")
    fun getAll(): Flow<List<PostEntity>>

    @Query("SELECT COUNT(*) == 0 FROM PostEntity")
    suspend fun isEmpty(): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE) //это для сохранения и редактирования, replace заменяет если есть такой же айди кажется
    suspend fun insert(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE) //это для getall
    suspend fun insert(posts: List<PostEntity>)

    @Query("DELETE FROM PostEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("""
                UPDATE PostEntity SET
                    likes = likes + CASE WHEN likedByMe THEN -1 ELSE 1 END,
                    LikedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
                WHERE id = :id;
            """)
    suspend fun likeById(id: Long)

    @Query("""
                UPDATE PostEntity SET                    
                    savedOnServer = CASE WHEN savedOnServer THEN 0 ELSE 1 END
                WHERE id = :id;
            """)
    suspend fun saveOnServerSwitch(id: Long)


    @Query("""
                UPDATE PostEntity SET
                    showed = 1 ;
            """)
    suspend fun showedSwitch()

    @Query("SELECT * FROM PostEntity WHERE id = :id")
    suspend fun getPostById(id: Long): PostEntity
}
