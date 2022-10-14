package com.karrar.movieapp.ui.allMedia

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.Pager
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.karrar.movieapp.data.repository.AllMediaFactory
import com.karrar.movieapp.domain.enums.AllMediaType
import com.karrar.movieapp.domain.models.Media
import com.karrar.movieapp.ui.UIState
import com.karrar.movieapp.ui.adapters.MediaInteractionListener
import com.karrar.movieapp.ui.base.BaseViewModel
import com.karrar.movieapp.utilities.Event
import com.karrar.movieapp.utilities.postEvent
import com.karrar.movieapp.utilities.toLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class AllMovieViewModel @Inject constructor(
    private val state: SavedStateHandle
) : BaseViewModel(), MediaInteractionListener {

    val args = AllMovieFragmentArgs.fromSavedStateHandle(state)

    @Inject
    lateinit var myFactory: AllMediaFactory
    private val dataSource by lazy { myFactory.create(actorID = args.id, type = args.type) }

    val allMedia: Flow<PagingData<Media>> =
        Pager(config = config, pagingSourceFactory = { dataSource }).flow.cachedIn(viewModelScope)

    private val _backEvent = MutableLiveData<Event<Boolean>>()
    val backEvent = _backEvent.toLiveData()

    private val _clickMovieEvent = MutableLiveData<Event<Int>>()
    val clickMovieEvent = _clickMovieEvent.toLiveData()

    private val _allMediaState = MutableLiveData<UIState<Boolean>>(UIState.Loading)
    val allMediaState = _allMediaState.toLiveData()

    private val _clickSeriesEvent = MutableLiveData<Event<Int>>()
    val clickSeriesEvent = _clickSeriesEvent.toLiveData()

    private val _clickRetryEvent = MutableLiveData<Event<Boolean>>()
    val clickRetryEvent = _clickRetryEvent.toLiveData()

    override fun getData() {
        _clickRetryEvent.postEvent(true)
    }

    override fun onClickMedia(mediaId: Int) {
        if (args.type == AllMediaType.ON_THE_AIR
            || args.type == AllMediaType.POPULAR
            || args.type == AllMediaType.AIRING_TODAY
            || args.type == AllMediaType.TOP_RATED) {
            _clickSeriesEvent.postEvent(mediaId)
        } else {
            _clickMovieEvent.postEvent(mediaId)
        }
    }

    fun setErrorUiState(loadState: LoadState) {
        when (loadState) {
            is LoadState.Error, null -> _allMediaState.postValue(UIState.Error(""))
            else -> {
                _allMediaState.postValue(UIState.Success(true))
            }
        }
    }
}