// Caminho: composeApp/src/commonMain/kotlin/com/pokedex/ui/screens/PokemonDetailScreen.kt
package com.pokedex.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pokedex.data.model.Pokemon
import com.pokedex.ui.components.*
import com.pokedex.viewmodel.DetailUiState
import com.pokedex.viewmodel.DetailViewModel
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

@Composable
fun PokemonDetailScreen(
    pokemonId: Int,
    viewModel: DetailViewModel
) {
    // Dispara a busca HTTP assim que a tela abre
    LaunchedEffect(pokemonId) { viewModel.load(pokemonId) }

    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is DetailUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is DetailUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("❌", fontSize = 48.sp)
                    Text(state.message, fontSize = 14.sp)
                }
            }
        }

        is DetailUiState.Success -> {
            DetailContent(
                pokemon = state.pokemon,
                isFavorite = state.isFavorite,
                onAddFavorite = { location -> viewModel.addToFavorites(state.pokemon, location) },
                onRemoveFavorite = { viewModel.removeFromFavorites(state.pokemon.id) }
            )
        }
    }
}

// ─── Conteúdo da tela ────────────────────────────────────────────────────────

@Composable
private fun DetailContent(
    pokemon: Pokemon,
    isFavorite: Boolean,
    onAddFavorite: (String) -> Unit,
    onRemoveFavorite: () -> Unit
) {
    val gradient = pokemonGradient(pokemon.types)
    var showCaptureDialog by remember { mutableStateOf(false) }

    if (showCaptureDialog) {
        CaptureLocationDialog(
            pokemonName = pokemon.name,
            onConfirm = { location ->
                showCaptureDialog = false
                onAddFavorite(location)
            },
            onDismiss = { showCaptureDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Hero header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(gradient)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(pokemon.name, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            pokemon.types.forEach { TypeChip(it) }
                        }
                    }
                    PokemonNumberBadge(pokemon.id)
                }
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    KamelImage(
                        resource = asyncPainterResource(pokemon.imageUrl),
                        contentDescription = pokemon.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(160.dp),
                        onLoading = { Text(getPokemonEmoji(pokemon.id), fontSize = 100.sp) },
                        onFailure = { Text(getPokemonEmoji(pokemon.id), fontSize = 100.sp) }
                    )
                }
            }
        }

        // Content
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {

            if (pokemon.description.isNotBlank()) {
                SectionTitle("Sobre")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = pokemon.description,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                        fontSize = 14.sp, lineHeight = 22.sp
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            SectionTitle("Dados")
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    InfoCard("⚖️ Peso", "${pokemon.weight} kg", Modifier.weight(1f))
                    InfoCard("📏 Altura", "${pokemon.height} m", Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    InfoCard("✨ Habilidade", pokemon.ability, Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            SectionTitle("Atributos Base")
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    StatBar("HP", pokemon.stats.hp)
                    StatBar("Ataque", pokemon.stats.attack)
                    StatBar("Defesa", pokemon.stats.defense)
                    StatBar("Atq. Especial", pokemon.stats.spAttack)
                    StatBar("Def. Especial", pokemon.stats.spDefense)
                    StatBar("Velocidade", pokemon.stats.speed)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (isFavorite) onRemoveFavorite()
                    else showCaptureDialog = true
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFavorite) Color(0xFF43A047) else MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Check else Icons.Default.Add,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isFavorite) "No Time! Toque para Remover" else "Adicionar ao Time",
                    fontWeight = FontWeight.Bold, fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ─── Diálogo de local de captura (nova regra de negócio M2) ──────────────────

@Composable
private fun CaptureLocationDialog(
    pokemonName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var location by remember { mutableStateOf("") }
    val cities = listOf(
        "Pallet Town", "Pewter City", "Cerulean City", "Vermilion City",
        "Lavender Town", "Celadon City", "Fuchsia City", "Saffron City",
        "Cinnabar Island", "Viridian City"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Onde você capturou $pokemonName?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Local de captura") },
                    placeholder = { Text("Ex: Pallet Town") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "Ou escolha uma cidade:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                // Chips de cidades rápidas
                cities.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { city ->
                            FilterChip(
                                selected = location == city,
                                onClick = { location = city },
                                label = { Text(city, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (location.isNotBlank()) onConfirm(location) },
                enabled = location.isNotBlank()
            ) { Text("Adicionar ao Time") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun InfoCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
