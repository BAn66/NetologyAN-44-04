package ru.netologia.nmedia.application

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NMediaApplication: Application() // Запускается самым первым если прописать в андроидманифесте//Даже пустой нужен для HILT
