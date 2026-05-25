// Caminho: composeApp/src/commonMain/kotlin/com/pokedex/data/remote/PokeApiModels.kt
package com.pokedex.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ─── Listagem ─────────────────────────────────────────────────────────────────

@Serializable
data class PokemonListResponse(
    val count: Int,
    val results: List<PokemonListItem>
)

@Serializable
data class PokemonListItem(
    val name: String,
    val url: String
) {
    /** Extrai o ID numérico da URL da PokeAPI. Ex: "…/pokemon/25/" → 25 */
    val id: Int get() = url.trimEnd('/').split('/').last().toInt()
}

// ─── Detalhe ──────────────────────────────────────────────────────────────────

@Serializable
data class PokemonDetailResponse(
    val id: Int,
    val name: String,
    val weight: Int,       // em hectogramas (÷10 = kg)
    val height: Int,       // em decímetros  (÷10 = m)
    val types: List<TypeSlot>,
    val abilities: List<AbilitySlot>,
    val stats: List<StatEntry>,
    val sprites: Sprites
)

@Serializable
data class TypeSlot(
    val slot: Int,
    val type: NamedResource
)

@Serializable
data class AbilitySlot(
    val ability: NamedResource,
    @SerialName("is_hidden") val isHidden: Boolean
)

@Serializable
data class StatEntry(
    @SerialName("base_stat") val baseStat: Int,
    val stat: NamedResource
)

@Serializable
data class Sprites(
    val other: OtherSprites? = null
)

@Serializable
data class OtherSprites(
    @SerialName("official-artwork") val officialArtwork: OfficialArtwork? = null
)

@Serializable
data class OfficialArtwork(
    @SerialName("front_default") val frontDefault: String? = null
)

@Serializable
data class NamedResource(val name: String)
