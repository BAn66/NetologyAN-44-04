package ru.netologia.nmedia.db

import android.content.Context
//import android.database.sqlite.SQLiteDatabase
//import android.database.sqlite.SQLiteOpenHelper
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ru.netologia.nmedia.dao.PostDao
//import ru.netologia.nmedia.dao.PostDaoImpl
import ru.netologia.nmedia.entity.PostEntity

@Database(entities = [PostEntity::class], version = 1)
abstract class AppDb : RoomDatabase(){
    abstract val postDao: PostDao

    companion object{
        @Volatile
        private var instance: AppDb? = null

        fun getInstance(context: Context): AppDb {
            return instance ?: synchronized(this){
                instance ?:
                    buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context, AppDb::class.java, "app.db")
                .allowMainThreadQueries()
                .build()
    }
}

//Для SQLite
//class AppDb private constructor(db : SQLiteDatabase){
//    val postDao: PostDao = PostDaoImpl(db)
//
//    companion object{
//        @Volatile
//        private var instance: AppDb? = null
//
//        fun getInstance(context: Context): AppDb {
//            return instance ?: synchronized(this){
//                instance ?: AppDb(
//                    buildDatabase(context, arrayOf(PostDaoImpl.DDL))
//                ).also { instance = it }
//            }
//        }
//
//        private fun buildDatabase(context: Context, DDLs: Array<String>) = DbHelper(
//            context, 1, "app.db", DDLs,
//        ).writableDatabase
//    }
//}
//
//
//class DbHelper(context: Context, dbVersion: Int, dbName: String, private val DDLs: Array<String>) :
//    SQLiteOpenHelper(context, dbName, null, dbVersion) {
//    override fun onCreate(db: SQLiteDatabase) {
//        DDLs.forEach {
//            db.execSQL(it)
//        }
//    }
//
//    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
//        TODO("Not implemented")
//    }
//
//    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
//        TODO("Not implemented")
//    }
//}