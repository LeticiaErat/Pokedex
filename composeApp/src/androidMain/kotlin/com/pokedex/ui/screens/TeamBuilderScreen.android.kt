// Caminho: composeApp/src/androidMain/kotlin/com/pokedex/ui/screens/TeamBuilderScreen.android.kt
package com.pokedex.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pokedex.data.local.PokemonFavoriteEntity
import com.pokedex.data.model.PokemonType
import com.pokedex.ui.components.getPokemonEmoji
import com.pokedex.ui.components.pokemonGradientFromNames
import com.pokedex.viewmodel.TeamViewModel
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

@Composable
actual fun TeamBuilderScreen(
    viewModel: TeamViewModel,
    onPokemonClick: (Int) -> Unit
) {
    val team by viewModel.team.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Meu Time", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("${team.size} Pokémon${if (team.size != 1) "s" else ""} capturado${if (team.size != 1) "s" else ""}",
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f), fontSize = 14.sp)
                    }
                    Icon(Icons.Default.Star, contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                }
            }
        }

        if (team.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(modifier = Modifier.size(120.dp), shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant) {
                        Box(contentAlignment = Alignment.Center) { Text("😴", fontSize = 52.sp) }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Seu time está vazio!", style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Vá até a Pokédex e adicione\nseus favoritos aqui.",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        fontSize = 14.sp, textAlign = TextAlign.Center)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(team, key = { it.id }) { entity ->
                    TeamPokemonCard(entity = entity, onClick = { onPokemonClick(entity.id) })
                }
            }
        }
    }
}

@Composable
private fun TeamPokemonCard(entity: PokemonFavoriteEntity, onClick: () -> Unit) {
    val typeNames = entity.types.split(",")
    val gradient = pokemonGradientFromNames(typeNames)

    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().background(gradient)) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(modifier = Modifier.size(64.dp), shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f)) {
                        Box(contentAlignment = Alignment.Center) {
                            KamelImage(
                                resource = asyncPainterResource(entity.imageUrl),
                                contentDescription = entity.name,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.size(52.dp),
                                onLoading = { Text(getPokemonEmoji(entity.id), fontSize = 28.sp) },
                                onFailure = { Text(getPokemonEmoji(entity.id), fontSize = 28.sp) }
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("#${entity.id.toString().padStart(3, '0')}",
                            color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                        Text(entity.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                // Local de captura
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "📍 ${entity.capturedLocation}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
