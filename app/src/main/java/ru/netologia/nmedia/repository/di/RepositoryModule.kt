package ru.netologia.nmedia.repository.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.netologia.nmedia.repository.PostRepository
import ru.netologia.nmedia.repository.PostRepositoryImpl
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
interface RepositoryModule { //это надо для HILT

    @Singleton
    @Binds
    fun bindsPostRepository(
        impl: PostRepositoryImpl
    ): PostRepository
}