package ru.netologia.nmedia.db

//Для ROOM

import android.content.Context
//import android.database.sqlite.SQLiteDatabase
//import android.database.sqlite.SQLiteOpenHelper
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ru.netologia.nmedia.dao.PostDao
//import ru.netologia.nmedia.dao.PostDaoImpl
import ru.netologia.nmedia.entity.PostEntity

@Database(entities = [PostEntity::class], version = 3, exportSchema = false)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao

    companion object {
        @Volatile
        private var instance: AppDb? = null

        fun getInstance(context: Context): AppDb {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        //Для рум с ипользованием корутинов
        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context, AppDb::class.java, "app.db")
//                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build()
    }

//        Только для рум
//        private fun buildDatabase(context: Context) =
//            Room.databaseBuilder(context, AppDb::class.java, "app.db")
//                .allowMainThreadQueries()
//                .build()
//    }

}
