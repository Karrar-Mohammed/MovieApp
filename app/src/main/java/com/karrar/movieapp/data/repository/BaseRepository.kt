package com.karrar.movieapp.data.repository

import androidx.paging.PagingConfig
import com.karrar.movieapp.data.remote.response.BaseListResponse
import kotlinx.datetime.Instant
import retrofit2.Response
import java.util.*
import kotlin.time.Duration.Companion.days

abstract class BaseRepository {

    val config = PagingConfig(pageSize = 100, prefetchDistance = 5, enablePlaceholders = false)

    protected suspend fun <I, O> wrap(
        function: suspend () -> Response<I>,
        mapper: (I) -> O
    ): O {
        val response = function()
        return if (response.isSuccessful) {
            response.body()?.let { mapper(it) } ?: throw Throwable()
        } else {
            throw Throwable("response is not successful")
        }
    }

    protected suspend fun <I, O> refreshWrapper(
        request: suspend () -> Response<BaseListResponse<I>>,
        mapper: (List<I>?) -> List<O>?,
        insertIntoDatabase: suspend (List<O>) -> Unit,
    ) {
        val response = request()
        if(response.isSuccessful){
            val items = response.body()?.items
            mapper(items)?.let { list ->
                insertIntoDatabase(list)
            }
        }else{
            throw  Throwable()
        }
    }

    protected suspend fun refreshOneTimePerDay(
        requestDate:Long?,
        refreshData :  suspend (Date) -> Unit
    ) {
        val currentDate = Date()

        if (shouldRefresh(currentDate.time, requestDate)) {
            refreshData(currentDate)
        }
    }

    private fun shouldRefresh(currentDateMilli: Long, requestDate: Long?): Boolean {
        return requestDate?.let {
            val currentDate = Instant.fromEpochMilliseconds(currentDateMilli)
            val refreshDate = Instant.fromEpochMilliseconds(requestDate).plus(1.days)

            return currentDate > refreshDate
        } ?: true
    }
}
