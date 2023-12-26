package ru.netologia.nmedia.application

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

//import ru.netologia.nmedia.auth.AppAuth
//import ru.netologia.nmedia.di.DependencyContainer

@HiltAndroidApp
class NMediaApplication: Application() // Запускается самым первым если прописать в андроидманифесте
//{
//    override fun onCreate() {
//        super.onCreate()
//        AppAuth.initAuth(this)
//        DependencyContainer.initApp(this) //Фабрика для создания зависимостей
//    }
//}