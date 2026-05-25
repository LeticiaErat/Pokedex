// Caminho: composeApp/src/commonMain/kotlin/com/pokedex/viewmodel/TeamViewModel.kt
package com.pokedex.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokedex.data.local.PokemonFavoriteEntity
import com.pokedex.data.repository.PokemonRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * Gerencia a tela "Meu Time".
 * Os dados vêm do Room via Flow, portanto são reativos e persistidos.
 */
class TeamViewModel(
    private val repository: PokemonRepository
) : ViewModel() {

    val team: StateFlow<List<PokemonFavoriteEntity>> =
        repository.getFavorites()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )
}
