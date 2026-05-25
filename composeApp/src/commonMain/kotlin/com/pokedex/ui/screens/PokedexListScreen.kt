// Caminho: composeApp/src/commonMain/kotlin/com/pokedex/ui/screens/PokedexListScreen.kt
package com.pokedex.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pokedex.data.model.PokemonType
import com.pokedex.ui.components.PokemonCacheCard
import com.pokedex.viewmodel.PokedexUiState
import com.pokedex.viewmodel.PokedexViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokedexListScreen(
    viewModel: PokedexViewModel,
    onPokemonClick: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val nameFilter by viewModel.nameFilter.collectAsState()
    val typeFilter by viewModel.typeFilter.collectAsState()
    var isSearchActive by remember { mutableStateOf(false) }
    var showTypeFilter by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── SearchBar ────────────────────────────────────────────────────────
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = nameFilter,
                        onQueryChange = { viewModel.updateNameFilter(it) },
                        onSearch = { isSearchActive = false },
                        expanded = isSearchActive,
                        onExpandedChange = { isSearchActive = it },
                        placeholder = { Text("Buscar Pokémon...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (nameFilter.isNotEmpty()) {
                                IconButton(onClick = { viewModel.updateNameFilter("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Limpar")
                                }
                            }
                        }
                    )
                },
                expanded = isSearchActive,
                onExpandedChange = { isSearchActive = it },
                modifier = Modifier.fillMaxWidth()
            ) {}
        }

        // ── Filtro por tipo ──────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = typeFilter.isEmpty(),
                onClick = { viewModel.updateTypeFilter("") },
                label = { Text("Todos") }
            )
            // Scroll horizontal dos tipos mais comuns
            val popularTypes = listOf(
                PokemonType.FIRE, PokemonType.WATER, PokemonType.GRASS,
                PokemonType.ELECTRIC, PokemonType.PSYCHIC, PokemonType.DRAGON,
                PokemonType.NORMAL, PokemonType.GHOST, PokemonType.FIGHTING
            )
            popularTypes.forEach { type ->
                FilterChip(
                    selected = typeFilter.equals(type.apiName, ignoreCase = true),
                    onClick = { viewModel.updateTypeFilter(type.apiName) },
                    label = { Text(type.displayName) }
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // ── Conteúdo principal ───────────────────────────────────────────────
        when (val state = uiState) {
            is PokedexUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Sincronizando Pokédex...", fontSize = 14.sp)
                    }
                }
            }

            is PokedexUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("❌", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(state.message, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.retry() }) { Text("Tentar novamente") }
                    }
                }
            }

            is PokedexUiState.Success -> {
                val gridState = rememberLazyGridState()

                // Detecta scroll chegando ao fim para carregar próxima página
                val shouldLoadMore by remember {
                    derivedStateOf {
                        val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                        val total = gridState.layoutInfo.totalItemsCount
                        lastVisible >= total - 4 && !state.isLoadingMore && !state.hasReachedEnd
                    }
                }
                LaunchedEffect(shouldLoadMore) {
                    if (shouldLoadMore) viewModel.loadNextPage()
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${state.items.size} Pokémon${if (state.items.size != 1) "s" else ""}",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (state.items.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("😶", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Nenhum Pokémon encontrado",
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                fontSize = 16.sp
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.items, key = { it.id }) { pokemon ->
                            PokemonCacheCard(
                                entity = pokemon,
                                onClick = { onPokemonClick(pokemon.id) }
                            )
                        }

                        // Indicador de carregamento no final da lista
                        if (state.isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
