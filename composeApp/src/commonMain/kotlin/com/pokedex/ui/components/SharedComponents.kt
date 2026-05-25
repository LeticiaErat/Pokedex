// Caminho: composeApp/src/commonMain/kotlin/com/pokedex/ui/components/SharedComponents.kt
package com.pokedex.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pokedex.data.model.PokemonType

// ─── Gradiente a partir de lista de PokemonType ──────────────────────────────

fun pokemonGradient(types: List<PokemonType>): Brush {
    val colors = if (types.isEmpty()) {
        listOf(Color(0xFFBDBDBD), Color(0xFF9E9E9E))
    } else if (types.size == 1) {
        val c = Color(types[0].colorHex)
        listOf(c, c.copy(alpha = 0.7f))
    } else {
        listOf(Color(types[0].colorHex), Color(types[1].colorHex))
    }
    return Brush.linearGradient(colors)
}

// ─── Gradiente a partir de nomes de tipo (string) – usado no cache/favoritos ─

fun pokemonGradientFromNames(typeNames: List<String>): Brush {
    val types = typeNames.mapNotNull { name ->
        PokemonType.entries.firstOrNull { it.apiName.equals(name, ignoreCase = true) }
    }
    return pokemonGradient(types)
}

// ─── Type chip ────────────────────────────────────────────────────────────────

@Composable
fun TypeChip(type: PokemonType) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(type.colorHex).copy(alpha = 0.3f)
    ) {
        Text(
            text = type.displayName,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// ─── Número badge ─────────────────────────────────────────────────────────────

@Composable
fun PokemonNumberBadge(id: Int) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.2f)
    ) {
        Text(
            text = "#${id.toString().padStart(3, '0')}",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

// ─── Section title ────────────────────────────────────────────────────────────

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

// ─── Stat bar ─────────────────────────────────────────────────────────────────

@Composable
fun StatBar(label: String, value: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.width(110.dp),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = value.toString(),
            modifier = Modifier.width(36.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        LinearProgressIndicator(
            progress = { (value / 255f).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = statColor(value),
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

private fun statColor(value: Int): Color = when {
    value >= 120 -> Color(0xFF43A047)
    value >= 80  -> Color(0xFFFDD835)
    else         -> Color(0xFFEF5350)
}

// ─── Emoji helper ─────────────────────────────────────────────────────────────

fun getPokemonEmoji(id: Int): String = when (id) {
    1   -> "🌱"; 4   -> "🔥"; 7   -> "💧"; 25  -> "⚡"
    39  -> "🎤"; 52  -> "🐱"; 94  -> "👻"; 130 -> "🐉"
    143 -> "😴"; 149 -> "🐲"; 150 -> "🧬"; 151 -> "✨"
    else -> "⭐"
}
