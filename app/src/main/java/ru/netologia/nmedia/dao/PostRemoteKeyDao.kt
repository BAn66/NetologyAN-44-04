package ru.netologia.nmedia.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.netologia.nmedia.entity.PostRemoteKeyEntity

@Dao
interface PostRemoteKeyDao {
    @Query("SELECT max(`key`) FROM PostRemoteKeyEntity") //поиск максимального айди среди постов
    suspend fun max(): Long? //Если данных может не быть в базе, можно сделать их опциональными поставив знак "?"

    @Query("SELECT min(`key`) FROM PostRemoteKeyEntity") //поиск минимального айди среди постов
    suspend fun min(): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE ) //Запись одного энтети
    suspend fun insert(postRemoteKeyEntity: PostRemoteKeyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE ) //Запись в бд листа энтети
    suspend fun insert(postRemoteKeyEntityList: List<PostRemoteKeyEntity>)

    @Query("DELETE FROM PostRemoteKeyEntity")
    suspend fun clear()

}