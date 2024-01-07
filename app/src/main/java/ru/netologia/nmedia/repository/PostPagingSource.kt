package ru.netologia.nmedia.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import retrofit2.HttpException
import ru.netologia.nmedia.api.ApiService
import ru.netologia.nmedia.dao.PostDao
import ru.netologia.nmedia.dto.Post
import ru.netologia.nmedia.entity.PostEntity
import ru.netologia.nmedia.error.ApiError
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    private val apiService: ApiService,
    private val postDao: PostDao, //база для записи результатов
) : RemoteMediator<Int, PostEntity>() {
    override suspend fun load(loadType: LoadType, state: PagingState<Int, PostEntity>): MediatorResult {
        try {
            val response = when (loadType) {
                LoadType.REFRESH -> { //обработка рефреша
                    apiService.getLatest(state.config.pageSize)
                }

                LoadType.PREPEND -> { //Обработка скролла вверх(новая страница не будет загружаться, мы сделали специально. Так в большинстве приложений сейчас)
                    val id =  state.firstItemOrNull()?.id ?: return MediatorResult.Success(false)
                    apiService.getAfter(id, state.config.pageSize)
                }

                LoadType.APPEND -> {//Обработка скролла вниз
                    val id =  state.lastItemOrNull()?.id ?: return MediatorResult.Success(false)
                    apiService.getBefore(id, state.config.pageSize)
                }
            }

            if (!response.isSuccessful) {
                throw HttpException(response)
            }
            val body = response.body()?: throw ApiError(
                response.code(),
                response.message(),
            )
            val nextKey = if(body.isEmpty()) null else body.last().id

            postDao.insert(body.map(PostEntity::fromDto))//записываем тело ответа на запрос в БД

            return MediatorResult.Success(
                body.isEmpty()
            )
        } catch (e: IOException) {
            return MediatorResult.Error(e)
        }
    }
}