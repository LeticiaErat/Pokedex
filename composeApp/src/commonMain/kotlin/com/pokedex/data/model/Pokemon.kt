// Caminho: composeApp/src/commonMain/kotlin/com/pokedex/data/model/Pokemon.kt
package com.pokedex.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Pokemon(
    val id: Int,
    val name: String,
    val types: List<PokemonType>,
    val description: String,
    val weight: Double,   // kg
    val height: Double,   // m
    val category: String,
    val ability: String,
    val stats: PokemonStats,
    val imageUrl: String
)

@Serializable
data class PokemonStats(
    val hp: Int,
    val attack: Int,
    val defense: Int,
    val spAttack: Int,
    val spDefense: Int,
    val speed: Int
)

/**
 * [apiName] é o nome exato retornado pela PokeAPI, usado no mapeamento DTO → Domain.
 */
enum class PokemonType(val displayName: String, val colorHex: Long, val apiName: String) {
    FIRE     ("Fogo",     0xFFFF6B35, "fire"),
    WATER    ("Água",     0xFF4FC3F7, "water"),
    GRASS    ("Grama",    0xFF66BB6A, "grass"),
    POISON   ("Veneno",   0xFFAB47BC, "poison"),
    FLYING   ("Voador",   0xFF90CAF9, "flying"),
    ELECTRIC ("Elétrico", 0xFFFFEE58, "electric"),
    PSYCHIC  ("Psíquico", 0xFFEC407A, "psychic"),
    ICE      ("Gelo",     0xFF80DEEA, "ice"),
    DRAGON   ("Dragão",   0xFF7986CB, "dragon"),
    DARK     ("Sombrio",  0xFF78909C, "dark"),
    FIGHTING ("Lutador",  0xFFEF5350, "fighting"),
    ROCK     ("Pedra",    0xFFBCAAA4, "rock"),
    GHOST    ("Fantasma", 0xFF7E57C2, "ghost"),
    BUG      ("Inseto",   0xFFD4E157, "bug"),
    STEEL    ("Aço",      0xFFB0BEC5, "steel"),
    NORMAL   ("Normal",   0xFFBDBDBD, "normal"),
    GROUND   ("Terra",    0xFFFFCC02, "ground"),
    FAIRY    ("Fada",     0xFFF48FB1, "fairy")
}
