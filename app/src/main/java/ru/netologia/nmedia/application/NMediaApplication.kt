package ru.netologia.nmedia.application

import android.app.Application
import ru.netologia.nmedia.auth.AppAuth

class NMediaApplication: Application() { // Запускается самым первым если прописать в андроидманифесте
    override fun onCreate() {
        super.onCreate()
        AppAuth.initAuth(this)

        //TODO REMOVE BEFORE RELeaze
        AppAuth.getInstance().setAuth(5, "x-token") //Временная заглушка
    }
}