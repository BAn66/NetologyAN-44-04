package ru.netologia.nmedia.db

//Для ROOM

//import android.content.Context
import androidx.room.Database
//import androidx.room.Room
import androidx.room.RoomDatabase
//import androidx.room.TypeConverters
import ru.netologia.nmedia.dao.PostDao
import ru.netologia.nmedia.entity.PostEntity

@Database(entities = [PostEntity::class], version = 3, exportSchema = false)
//@TypeConverters (Converters::class) //Не знаю зачем это было, было в лекции не пригодилось
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao


    // После переноса в депенденси контейнер больше не нужен
//    companion object {
//        @Volatile
//        private var instance: AppDb? = null
//
//        fun getInstance(context: Context): AppDb {
//            return instance ?: synchronized(this) {
//                instance ?: buildDatabase(context).also { instance = it }
//            }
//        }

        //Для рум с ипользованием корутинов - перенесли в DependencyContainer в контейнер зависимостей
//        private fun buildDatabase(context: Context) =
//            Room.databaseBuilder(context, AppDb::class.java, "app.db")
////                .allowMainThreadQueries()
//                .fallbackToDestructiveMigration()
//                .build()
//    }

//        Только для рум
//        private fun buildDatabase(context: Context) =
//            Room.databaseBuilder(context, AppDb::class.java, "app.db")
//                .allowMainThreadQueries()
//                .build()
//    }

}
