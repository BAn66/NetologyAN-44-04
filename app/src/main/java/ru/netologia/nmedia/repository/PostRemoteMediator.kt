package ru.netologia.nmedia.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import kotlinx.coroutines.delay
import ru.netologia.nmedia.api.ApiService
import ru.netologia.nmedia.dao.PostDao
import ru.netologia.nmedia.dao.PostRemoteKeyDao
import ru.netologia.nmedia.db.AppDb
import ru.netologia.nmedia.entity.PostEntity
import ru.netologia.nmedia.entity.PostRemoteKeyEntity
import ru.netologia.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    private val apiService: ApiService,
    private val postDao: PostDao, //база для записи результатов
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val appDb: AppDb,
) : RemoteMediator<Int, PostEntity>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {
        try {
            val response = when (loadType) {
                LoadType.REFRESH -> { //обработка рефреша
                    if(postDao.isEmpty()) {
                        apiService.getLatest(state.config.initialLoadSize)
                    } else {
                        val id = postRemoteKeyDao.max() ?: return MediatorResult.Success(false)
                        apiService.getAfter(id, state.config.pageSize)
                    }
                }

                LoadType.PREPEND -> { //Обработка скролла вверх(новая страница не будет загружаться, мы сделали специально true препенд выключен, false препенд включен.
                    // Так в большинстве приложений сейчас)
//                    delay(2500)
                    val id = postRemoteKeyDao.max() ?: return MediatorResult.Success(false)
                    apiService.getAfter(id, state.config.pageSize)
                }

                LoadType.APPEND -> {//Обработка скролла вниз
                    val id = postRemoteKeyDao.min() ?: return MediatorResult.Success(false)
                    apiService.getBefore(id, state.config.pageSize)
                }
            }

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())

            //заполняем базу ключей данными которые приходят из сети используя транзакции
            appDb.withTransaction {
                when (loadType) {
                    LoadType.REFRESH -> {
//                        postRemoteKeyDao.clear()
                        if(postDao.isEmpty()){
                            postRemoteKeyDao.insert(
                                listOf(
                                    PostRemoteKeyEntity(
                                        PostRemoteKeyEntity.KeyType.AFTER,
                                        body.first().id
                                    ),
                                    PostRemoteKeyEntity(
                                        PostRemoteKeyEntity.KeyType.BEFORE,
                                        body.last().id

                                    )
                                )
                            )
                        }
                        else{
                            postRemoteKeyDao.insert(
                                PostRemoteKeyEntity(
                                    PostRemoteKeyEntity.KeyType.AFTER,
                                    body.first().id
                                )
                            )
                        }
                    //postDao.clear()
                    }

                    LoadType.PREPEND ->   //ветка недостижима если отключить препенд наверху
                    {//Обработка скролла вверх
                        postRemoteKeyDao.insert(
                            PostRemoteKeyEntity(
                                PostRemoteKeyEntity.KeyType.AFTER,
                                body.first().id
                            )
                        )
                    }


                    LoadType.APPEND -> {//Обработка скролла вниз

                        postRemoteKeyDao.insert(
                            PostRemoteKeyEntity(
                                PostRemoteKeyEntity.KeyType.BEFORE,
                                body.last().id
                            )
                        )
                    }
                    else -> Unit
                }
                postDao.insert(body.toEntity())
            }

//            val nextKey = if (body.isEmpty()) null else body.last().id

//            postDao.insert(body.map(PostEntity::fromDto))//записываем тело ответа на запрос в БД

            return MediatorResult.Success(
                body.isEmpty()
            )
        } catch (e: IOException) {
            return MediatorResult.Error(e)
        }
    }
}