// Caminho: composeApp/src/commonMain/kotlin/com/pokedex/App.kt
package com.pokedex

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.pokedex.data.local.AppDatabase
import com.pokedex.data.local.buildDatabase
import com.pokedex.data.local.getDatabaseBuilder
import com.pokedex.data.repository.PokemonRepository
import com.pokedex.navigation.Screen
import com.pokedex.ui.screens.*
import com.pokedex.ui.theme.PokedexTheme
import com.pokedex.viewmodel.DetailViewModel
import com.pokedex.viewmodel.PokedexViewModel
import com.pokedex.viewmodel.TeamViewModel

/**
 * Ponto de entrada do Compose.
 * [context] é passado pela plataforma (Activity no Android, null no iOS).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(context: Any? = null) {
    PokedexTheme {
        // ── Dependências (composição simples sem DI externo) ─────────────────
        val db: AppDatabase = remember(context) {
            buildDatabase(getDatabaseBuilder(context))
        }
        val repository = remember(db) { PokemonRepository(db) }

        // ── ViewModels ───────────────────────────────────────────────────────
        val pokedexViewModel: PokedexViewModel = viewModel { PokedexViewModel(repository) }
        val teamViewModel: TeamViewModel       = viewModel { TeamViewModel(repository) }

        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        val showBottomBar = currentDestination?.let {
            it.hasRoute(Screen.PokedexList::class) || it.hasRoute(Screen.TeamBuilder::class)
        } ?: false

        val topBarTitle = when {
            currentDestination?.hasRoute(Screen.Home::class) == true        -> "Pokédex"
            currentDestination?.hasRoute(Screen.PokedexList::class) == true -> "Pokédex"
            currentDestination?.hasRoute(Screen.TeamBuilder::class) == true -> "Meu Time"
            else -> "Detalhes"
        }

        val showTopBar = currentDestination?.hasRoute(Screen.Home::class) != true

        Scaffold(
            topBar = {
                if (showTopBar) {
                    TopAppBar(
                        title = { Text(topBarTitle) },
                        navigationIcon = {
                            if (currentDestination?.hasRoute(Screen.PokemonDetail::class) == true) {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Text("←", style = MaterialTheme.typography.titleLarge)
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            },
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                        NavigationBarItem(
                            selected = currentDestination?.hasRoute(Screen.PokedexList::class) == true,
                            onClick = {
                                navController.navigate(Screen.PokedexList) {
                                    popUpTo(Screen.PokedexList) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(Icons.Default.List, contentDescription = "Pokédex") },
                            label = { Text("Pokédex") }
                        )
                        NavigationBarItem(
                            selected = currentDestination?.hasRoute(Screen.TeamBuilder::class) == true,
                            onClick = {
                                navController.navigate(Screen.TeamBuilder) {
                                    popUpTo(Screen.PokedexList) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(Icons.Default.Favorite, contentDescription = "Meu Time") },
                            label = { Text("Meu Time") }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable<Screen.Home> {
                    HomeScreen(
                        onNavigateToPokedex = { navController.navigate(Screen.PokedexList) },
                        onNavigateToTeam = { navController.navigate(Screen.TeamBuilder) }
                    )
                }
                composable<Screen.PokedexList> {
                    PokedexListScreen(
                        viewModel = pokedexViewModel,
                        onPokemonClick = { id -> navController.navigate(Screen.PokemonDetail(id)) }
                    )
                }
                composable<Screen.PokemonDetail> { backStackEntry ->
                    val route = backStackEntry.toRoute<Screen.PokemonDetail>()
                    // DetailViewModel é escopado por pokemonId para não reusar estado stale
                    val detailViewModel: DetailViewModel = viewModel(
                        key = "detail_${route.pokemonId}"
                    ) { DetailViewModel(repository) }
                    PokemonDetailScreen(
                        pokemonId = route.pokemonId,
                        viewModel = detailViewModel
                    )
                }
                composable<Screen.TeamBuilder> {
                    TeamBuilderScreen(
                        viewModel = teamViewModel,
                        onPokemonClick = { id -> navController.navigate(Screen.PokemonDetail(id)) }
                    )
                }
            }
        }
    }
}
