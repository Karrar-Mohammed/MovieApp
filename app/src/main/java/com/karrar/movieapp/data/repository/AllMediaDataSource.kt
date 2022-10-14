package com.karrar.movieapp.data.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.karrar.movieapp.domain.enums.MovieType
import com.karrar.movieapp.domain.models.Media
import com.karrar.movieapp.utilities.Constants
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject


//where should I put this interface ?!!!
@AssistedFactory
interface AllMediaFactory {
    fun create(actorID: Int, type: MovieType): AllMediaDataSource
}

class AllMediaDataSource @AssistedInject constructor(
    private val repository: MovieRepository,
    private val seriesRepository: SeriesRepository,
    @Assisted private val type: MovieType,
    @Assisted private val actorID: Int
) : PagingSource<Int, Media>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Media> {
        val pageNumber = params.key ?: 1
        return try {

            val response = when (type) {
                MovieType.TRENDING -> repository.getTrendingMovies(pageNumber)
                MovieType.UPCOMING -> repository.getUpcomingMovies(pageNumber)
                MovieType.NOW_STREAMING -> repository.getNowPlayingMovies(pageNumber)
                MovieType.MYSTERY -> {
                    repository.getMovieListByGenreID(Constants.MYSTERY_ID, pageNumber)
                }
                MovieType.ADVENTURE -> {
                    repository.getMovieListByGenreID(Constants.ADVENTURE_ID, pageNumber)
                }
                MovieType.ON_THE_AIR -> {
                    seriesRepository.getOnTheAir(pageNumber)
                }
                MovieType.NON -> {
                    if (pageNumber == 1) {
                        repository.getActorMovies(actorID)
                    } else {
                        emptyList()
                    }
                }
            }

            LoadResult.Page(
                data = response,
                prevKey = if (response.isEmpty()) null else pageNumber - 1,
                nextKey = if (response.isEmpty()) null else pageNumber + 1,
                itemsBefore = LoadResult.Page.COUNT_UNDEFINED,
                itemsAfter = LoadResult.Page.COUNT_UNDEFINED
            )
        } catch (e: Throwable) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Media>): Int? {
        return state.anchorPosition
    }
}
