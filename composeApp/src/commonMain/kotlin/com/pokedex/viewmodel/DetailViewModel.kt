// Caminho: composeApp/src/commonMain/kotlin/com/pokedex/viewmodel/DetailViewModel.kt
package com.pokedex.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokedex.data.model.Pokemon
import com.pokedex.data.repository.PokemonRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ─── Estado da UI ─────────────────────────────────────────────────────────────

sealed class DetailUiState {
    object Loading : DetailUiState()
    data class Success(val pokemon: Pokemon, val isFavorite: Boolean) : DetailUiState()
    data class Error(val message: String) : DetailUiState()
}

// ─── ViewModel ────────────────────────────────────────────────────────────────

class DetailViewModel(
    private val repository: PokemonRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    /**
     * Disparado pela tela ao navegar. Sempre faz requisição HTTP à PokeAPI
     * conforme exigido pelo enunciado (dados frescos na tela de detalhes).
     */
    fun load(pokemonId: Int) {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            try {
                val pokemon = repository.fetchDetail(pokemonId)
                val isFav = repository.isFavorite(pokemonId)
                _uiState.value = DetailUiState.Success(pokemon, isFav)
            } catch (e: Exception) {
                _uiState.value = DetailUiState.Error(e.message ?: "Erro ao carregar detalhes")
            }
        }
    }

    /**
     * Adiciona ao time/favoritos com o local de captura obrigatório.
     */
    fun addToFavorites(pokemon: Pokemon, capturedLocation: String) {
        viewModelScope.launch {
            repository.addFavorite(pokemon, capturedLocation)
            _uiState.value = DetailUiState.Success(pokemon, isFavorite = true)
        }
    }

    fun removeFromFavorites(pokemonId: Int) {
        viewModelScope.launch {
            repository.removeFavorite(pokemonId)
            val state = _uiState.value as? DetailUiState.Success ?: return@launch
            _uiState.value = state.copy(isFavorite = false)
        }
    }
}
