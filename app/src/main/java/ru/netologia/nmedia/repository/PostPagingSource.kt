package ru.netologia.nmedia.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import retrofit2.HttpException
import ru.netologia.nmedia.api.ApiService
import ru.netologia.nmedia.dto.Post
import java.io.IOException

class PostPagingSource(
    private val apiService: ApiService
) : PagingSource<Long, Post>() {
    override fun getRefreshKey(state: PagingState<Long, Post>): Long? = null

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, Post> {
        try {


            val result = when (params) {
                is LoadParams.Refresh -> { //обработка рефреша
                    apiService.getLatest(params.loadSize)
                }

                is LoadParams.Append -> {
                    apiService.getBefore(id = params.key, count = params.loadSize)
                }//Обработка скролла вниз
                is LoadParams.Prepend -> return LoadResult.Page(//Обработка скролла вверх(новая страница не будет загружаться, мы сделали специально. Так в большинстве приложений сейчас)
                    data = emptyList(),
                    nextKey = null,
                    prevKey = params.key //Если не хотим чтобы обрабатывался препенд
                )
            }

            if (!result.isSuccessful) {
                throw HttpException(result)
            }

            val data = result.body().orEmpty()
            return LoadResult.Page(data, prevKey = params.key, nextKey = data.lastOrNull()?.id)
        } catch (e: IOException) {
            return LoadResult.Error(e)
        }
    }
}