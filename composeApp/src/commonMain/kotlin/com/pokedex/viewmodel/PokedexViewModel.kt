// Caminho: composeApp/src/commonMain/kotlin/com/pokedex/viewmodel/PokedexViewModel.kt
package com.pokedex.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokedex.data.local.PokemonCacheEntity
import com.pokedex.data.repository.PokemonRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ─── Estado da UI ─────────────────────────────────────────────────────────────

sealed class PokedexUiState {
    object Loading : PokedexUiState()
    data class Success(
        val items: List<PokemonCacheEntity>,
        val isLoadingMore: Boolean = false,
        val hasReachedEnd: Boolean = false
    ) : PokedexUiState()
    data class Error(val message: String) : PokedexUiState()
}

// ─── ViewModel ────────────────────────────────────────────────────────────────

class PokedexViewModel(
    private val repository: PokemonRepository
) : ViewModel() {

    private val PAGE_SIZE = 20

    private val _uiState = MutableStateFlow<PokedexUiState>(PokedexUiState.Loading)
    val uiState: StateFlow<PokedexUiState> = _uiState.asStateFlow()

    private val _nameFilter = MutableStateFlow("")
    val nameFilter: StateFlow<String> = _nameFilter.asStateFlow()

    private val _typeFilter = MutableStateFlow("")
    val typeFilter: StateFlow<String> = _typeFilter.asStateFlow()

    private var currentPage = 0
    private var searchJob: Job? = null

    init {
        syncAndLoad()
    }

    // ─── Sincronização inicial ────────────────────────────────────────────────

    private fun syncAndLoad() {
        viewModelScope.launch {
            _uiState.value = PokedexUiState.Loading
            try {
                repository.syncIfNeeded()
                currentPage = 0
                val firstPage = repository.getPage(
                    nameFilter = _nameFilter.value,
                    typeFilter = _typeFilter.value,
                    page = 0,
                    pageSize = PAGE_SIZE
                )
                _uiState.value = PokedexUiState.Success(
                    items = firstPage,
                    hasReachedEnd = firstPage.size < PAGE_SIZE
                )
            } catch (e: Exception) {
                _uiState.value = PokedexUiState.Error(
                    e.message ?: "Erro ao carregar Pokémons"
                )
            }
        }
    }

    // ─── Paginação on-demand (scroll) ─────────────────────────────────────────

    fun loadNextPage() {
        val state = _uiState.value as? PokedexUiState.Success ?: return
        if (state.isLoadingMore || state.hasReachedEnd) return

        viewModelScope.launch {
            _uiState.update {
                (it as? PokedexUiState.Success)?.copy(isLoadingMore = true) ?: it
            }
            try {
                currentPage++
                val nextPage = repository.getPage(
                    nameFilter = _nameFilter.value,
                    typeFilter = _typeFilter.value,
                    page = currentPage,
                    pageSize = PAGE_SIZE
                )
                val current = (uiState.value as? PokedexUiState.Success)?.items ?: emptyList()
                _uiState.value = PokedexUiState.Success(
                    items = current + nextPage,
                    isLoadingMore = false,
                    hasReachedEnd = nextPage.size < PAGE_SIZE
                )
            } catch (e: Exception) {
                _uiState.update {
                    (it as? PokedexUiState.Success)?.copy(isLoadingMore = false) ?: it
                }
            }
        }
    }

    // ─── Filtros ──────────────────────────────────────────────────────────────

    fun updateNameFilter(query: String) {
        _nameFilter.value = query
        scheduleFilteredReload()
    }

    fun updateTypeFilter(type: String) {
        _typeFilter.value = type
        scheduleFilteredReload()
    }

    /** Debounce de 300 ms para não disparar query a cada tecla. */
    private fun scheduleFilteredReload() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            currentPage = 0
            val page = repository.getPage(
                nameFilter = _nameFilter.value,
                typeFilter = _typeFilter.value,
                page = 0,
                pageSize = PAGE_SIZE
            )
            _uiState.value = PokedexUiState.Success(
                items = page,
                hasReachedEnd = page.size < PAGE_SIZE
            )
        }
    }

    fun retry() = syncAndLoad()
}
