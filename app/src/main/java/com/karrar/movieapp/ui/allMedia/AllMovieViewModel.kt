package com.karrar.movieapp.ui.allMedia

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.map
import com.karrar.movieapp.domain.usecase.allMedia.CheckIfMediaIsSeriesUseCase
import com.karrar.movieapp.domain.usecase.allMedia.GetMediaByTypeUseCase
import com.karrar.movieapp.ui.adapters.MediaInteractionListener
import com.karrar.movieapp.ui.base.BaseViewModel
import com.karrar.movieapp.ui.mappers.MediaUiMapper
import com.karrar.movieapp.utilities.Event
import com.karrar.movieapp.utilities.toStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllMovieViewModel @Inject constructor(
    state: SavedStateHandle,
    private val checkIfMediaIsSeriesUseCase: CheckIfMediaIsSeriesUseCase,
    private val getMediaByType: GetMediaByTypeUseCase,
    private val mediaUiMapper: MediaUiMapper,
) : BaseViewModel(), MediaInteractionListener {

    val args = AllMovieFragmentArgs.fromSavedStateHandle(state)

    private val _uiState = MutableStateFlow(AllMediaUiState())
    val uiState = _uiState.toStateFlow()

    val mediaUIEvent: MutableStateFlow<Event<MediaUIEvent>?> = MutableStateFlow(null)

    init {
        getData()
    }

    override fun getData() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val allMediaItems =
                getMediaByType(
                    args.type,
                    args.id
                ).map { pager -> pager.map { mediaUiMapper.map(it) } }
            _uiState.update { it.copy(allMedia = allMediaItems, isLoading = false) }
        }

        mediaUIEvent.update { Event(MediaUIEvent.RetryEvent) }
    }

    override fun onClickMedia(mediaId: Int) {
        if (checkIfMediaIsSeriesUseCase(args.type)) {
            mediaUIEvent.update { Event(MediaUIEvent.ClickSeriesEvent(mediaId)) }
        } else {
            mediaUIEvent.update { Event(MediaUIEvent.ClickMovieEvent(mediaId)) }
        }
    }

    fun setErrorUiState(combinedLoadStates: CombinedLoadStates) {
        when (combinedLoadStates.refresh) {
            is LoadState.NotLoading -> {
                _uiState.update {
                    it.copy(isLoading = false, error = emptyList())
                }
            }
            LoadState.Loading -> {
                _uiState.update {
                    it.copy(isLoading = true, error = emptyList())
                }
            }
            is LoadState.Error -> {
                _uiState.update {
                    it.copy(isLoading = false, error = listOf(Error(404, "")))
                }
            }
        }

    }
}