package ru.netologia.nmedia.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.netologia.nmedia.dao.PostDao
import ru.netologia.nmedia.dao.PostRemoteKeyDao
import ru.netologia.nmedia.entity.PostEntity
import ru.netologia.nmedia.entity.PostRemoteKeyEntity

@Database(
    entities =
    [PostEntity::class, PostRemoteKeyEntity::class],
    version = 3,
    exportSchema = false
)

abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun postRemoteKeyDao(): PostRemoteKeyDao
}
