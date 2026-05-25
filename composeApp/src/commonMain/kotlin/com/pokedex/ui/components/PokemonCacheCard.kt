// Caminho: composeApp/src/commonMain/kotlin/com/pokedex/ui/components/PokemonCacheCard.kt
package com.pokedex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pokedex.data.local.PokemonCacheEntity
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

@Composable
fun PokemonCacheCard(
    entity: PokemonCacheEntity,
    onClick: () -> Unit
) {
    val typeNames = entity.types.split(",").filter { it.isNotBlank() }
    val gradient = pokemonGradientFromNames(typeNames)
    val imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/${entity.id}.png"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.75f)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "#${entity.id.toString().padStart(3, '0')}",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
                // Imagem carregada via URL oficial
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    KamelImage(
                        resource = asyncPainterResource(imageUrl),
                        contentDescription = entity.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(90.dp),
                        onLoading = { Text(getPokemonEmoji(entity.id), fontSize = 56.sp) },
                        onFailure = { Text(getPokemonEmoji(entity.id), fontSize = 56.sp) }
                    )
                }
                Text(
                    text = entity.name.replaceFirstChar { it.uppercase() },
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}
