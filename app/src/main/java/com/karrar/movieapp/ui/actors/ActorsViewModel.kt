package com.karrar.movieapp.ui.actors


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.map
import com.karrar.movieapp.domain.usecase.GetActorsDataUseCase
import com.karrar.movieapp.ui.actors.models.ActorsUIState
import com.karrar.movieapp.ui.adapters.ActorsInteractionListener
import com.karrar.movieapp.ui.base.BaseViewModel
import com.karrar.movieapp.ui.mappers.ActorUiMapper
import com.karrar.movieapp.utilities.Event
import com.karrar.movieapp.utilities.postEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActorsViewModel @Inject constructor(
    private val getActorsDataUseCase: GetActorsDataUseCase,
    private val actorMapper: ActorUiMapper
) : BaseViewModel(), ActorsInteractionListener {

    private val _uiState = MutableStateFlow(ActorsUIState())
    val uiState = _uiState.asStateFlow()

    val actorsUIEventFlow: MutableStateFlow<Event<ActorsUIEvent>?> = MutableStateFlow(null)

    init {
        getData()
    }

    override fun getData() {
        _uiState.update { it.copy(isLoading = true) }
        getActors()
        actorsUIEventFlow.update { Event(ActorsUIEvent.RetryEvent) }
    }

    private fun getActors() {
        viewModelScope.launch {
            val actorsItems =
                getActorsDataUseCase().map { pager -> pager.map { actorMapper.map(it) } }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    actors = actorsItems,
                    error = emptyList()
                )
            }
        }
    }

    override fun onClickActor(actorID: Int) {
        actorsUIEventFlow.update { Event(ActorsUIEvent.ActorEvent(actorID)) }
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
                    it.copy(isLoading = false, error = listOf(Error("")))
                }
            }
        }
    }

}